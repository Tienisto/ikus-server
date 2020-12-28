package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.AudioFileDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.AudioFile
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.moveDownItem
import de.ovgu.ikus.utils.moveUpItem
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.trimOrNull
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/audio/file")
class AudioFileController (
    private val logService: LogService,
    private val cacheService: CacheService,
    private val audioService: AudioService,
    private val audioFileService: AudioFileService
) {

    @PostMapping
    suspend fun createAudioFile(authentication: Authentication, @RequestBody request: Request.CreateAudioFile): AudioFileDto {
        val audio = audioService.findById(request.audioId) ?: throw ErrorCode(404, "Audio not found")
        val maxPosition = audioFileService.findMaxPosition()
        val file = AudioFile(
            audioId = audio.id,
            name = request.name.en.trim(), nameDe = request.name.de.trim(),
            text = request.text?.en?.trimOrNull(), textDe = request.text?.de?.trimOrNull(),
            position = maxPosition + 1
        )

        val savedFile = audioFileService.save(file)
        logService.log(LogType.CREATE_AUDIO_FILE, authentication.toUser(), "${audio.name} / ${file.name}")
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)

        return savedFile.toDto()
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
            .findAllOrdered()
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
            .findAllOrdered()
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
    suspend fun upload(@RequestParam fileId: Int, @RequestParam locale: IkusLocale,
                       @RequestPart("file") file: Mono<FilePart>) {
        val audioFile = audioFileService.findById(fileId) ?: throw ErrorCode(404, "Audio File not found")
        audioFileService.setAudio(audioFile, file.awaitFirst(), locale)
    }
}