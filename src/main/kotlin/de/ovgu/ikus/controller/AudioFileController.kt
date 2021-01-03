package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.dto.Token
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.AudioFile
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.*
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/audio/file")
class AudioFileController (
    private val logService: LogService,
    private val cacheService: CacheService,
    private val audioService: AudioService,
    private val audioFileService: AudioFileService
) {

    private class FileWithName(val name: String, val content: ByteArray)
    private class CreateRequest(var audioFile: AudioFile, var audioEn: FileWithName?, var audioDe: FileWithName?, val timestamp: Instant)
    private var requests = mapOf<String, CreateRequest>()

    @PostMapping
    suspend fun createAudioFile(authentication: Authentication, @RequestBody request: Request.CreateAudioFile): Token {
        val audio = audioService.findById(request.audioId) ?: throw ErrorCode(404, "Audio not found")
        val maxPosition = audioFileService.findMaxPosition()
        val file = AudioFile(
            audioId = audio.id,
            name = request.name.en.trim(), nameDe = request.name.de.trim(),
            text = request.text?.en?.trimOrNull(), textDe = request.text?.de?.trimOrNull(),
            file = "", fileDe = "", // will be added later
            position = maxPosition + 1
        )

        val token = UUID.randomUUID().toString()

        requests = requests
            .filterValues { createRequest -> Duration.between(createRequest.timestamp, Instant.now()) < Duration.ofMinutes(5) }
            .plus(token to CreateRequest(file, null, null, Instant.now()))

        return Token(token)
    }

    @PutMapping
    suspend fun updateAudioFile(authentication: Authentication, @RequestBody request: Request.UpdateAudioFile) {
        val file = audioFileService.findById(request.id) ?: throw ErrorCode(404, "Audio File not found")
        val audio = audioService.findById(request.audioId) ?: throw ErrorCode(404, "Audio not found")

        // apply
        file.audioId = audio.id
        file.name = request.name.en.trim()
        file.nameDe = request.name.de.trim()
        file.text = request.text?.en?.trimOrNull()
        file.textDe = request.text?.de?.trimOrNull()

        audioFileService.save(file)
        logService.log(LogType.UPDATE_AUDIO_FILE, authentication.toUser(), "${audio.name} / ${file.name}")
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = audioFileService.findById(request.id) ?: throw ErrorCode(404, "Audio File not found")
        val files = audioFileService
            .findByAudioOrderByPosition(file.audioId)
            .moveUpItem(item = file, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (files != null) {
            audioFileService.saveAll(files)
            cacheService.triggerUpdateFlag(CacheKey.AUDIO)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = audioFileService.findById(request.id) ?: throw ErrorCode(404, "Audio File not found")
        val files = audioFileService
            .findByAudioOrderByPosition(file.audioId)
            .moveDownItem(item = file, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (files != null) {
            audioFileService.saveAll(files)
            cacheService.triggerUpdateFlag(CacheKey.AUDIO)
        }
    }

    @DeleteMapping
    suspend fun deleteAudioFile(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = audioFileService.findById(request.id) ?: throw ErrorCode(404, "Audio File not found")
        audioFileService.delete(file)
        logService.log(LogType.DELETE_AUDIO_FILE, authentication.toUser(), file.name)
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)
    }

    @PutMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(authentication: Authentication,
                       @RequestParam(required = false) fileId: Int?, @RequestParam locale: IkusLocale,
                       @RequestParam(required = false) token: String?,
                       @RequestPart("file") file: Mono<FilePart>) {

        if (token != null) {
            val createRequest = requests[token] ?: throw ErrorCode(400, "invalid token")
            val filePart = file.awaitFirst()
            when (locale) {
                IkusLocale.EN -> createRequest.audioEn = FileWithName(filePart.filename(), filePart.toBytes())
                IkusLocale.DE -> createRequest.audioDe = FileWithName(filePart.filename(), filePart.toBytes())
            }

            val audioEn = createRequest.audioEn
            val audioDe = createRequest.audioDe

            if (audioEn != null && audioDe != null) {
                // both languages has been uploaded, create
                val savedFile = audioFileService.save(createRequest.audioFile)
                audioFileService.setAudio(savedFile, audioEn.name, audioEn.content, IkusLocale.EN)
                audioFileService.setAudio(savedFile, audioDe.name, audioDe.content, IkusLocale.DE)
                logService.log(LogType.CREATE_AUDIO_FILE, authentication.toUser(), "${createRequest.audioFile.name} / ${createRequest.audioFile.name}")
                cacheService.triggerUpdateFlag(CacheKey.AUDIO)
                requests = requests.minus(token)
            }

            return
        }

        if (fileId != null) {
            val audioFile = audioFileService.findById(fileId) ?: throw ErrorCode(404, "Audio File not found")
            val filePart = file.awaitFirst()
            audioFileService.setAudio(audioFile, filePart.filename(), filePart.toBytes(), locale)
            return
        }

        throw ErrorCode(400, "missing parameters")
    }
}