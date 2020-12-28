package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.AudioDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.Audio
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.moveDownItem
import de.ovgu.ikus.utils.moveUpItem
import de.ovgu.ikus.utils.toDto
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/audio")
class AudioController (
    private val logService: LogService,
    private val cacheService: CacheService,
    private val audioService: AudioService,
    private val audioFileService: AudioFileService
) {

    @GetMapping
    suspend fun getAudio(): List<AudioDto> {
        val files = audioFileService.findAllOrdered()
        return audioService
            .findAllOrdered()
            .map { audio ->
                val currFiles = files
                    .filter { file -> file.audioId == audio.id }
                    .map { file -> file.toDto() }

                audio.toDto(currFiles)
            }
    }

    @PostMapping
    suspend fun createAudio(authentication: Authentication, @RequestBody request: Request.CreateAudio): AudioDto {
        val maxPosition = audioService.findMaxPosition()
        val audio = Audio(
            name = request.name.en.trim(), nameDe = request.name.de.trim(),
            position = maxPosition + 1
        )

        val savedAudio = audioService.save(audio)
        logService.log(LogType.CREATE_AUDIO, authentication.toUser(), "${audio.name} (${audio.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)

        return savedAudio.toDto(emptyList())
    }

    @PutMapping
    suspend fun updateAudio(authentication: Authentication, @RequestBody request: Request.UpdateAudio) {
        val audio = audioService.findById(request.id) ?: throw ErrorCode(404, "Audio not found")

        // apply
        audio.name = request.name.en.trim()
        audio.nameDe = request.name.de.trim()

        audioService.save(audio)
        logService.log(LogType.UPDATE_AUDIO, authentication.toUser(), "${audio.name} (${audio.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val audio = audioService.findById(request.id) ?: throw ErrorCode(404, "Audio not found")
        val audioList = audioService
                .findAllOrdered()
                .moveUpItem(item = audio, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (audioList != null) {
            audioService.saveAll(audioList)
            cacheService.triggerUpdateFlag(CacheKey.AUDIO)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val audio = audioService.findById(request.id) ?: throw ErrorCode(404, "Audio not found")
        val audioList = audioService
            .findAllOrdered()
            .moveDownItem(item = audio, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (audioList != null) {
            audioService.saveAll(audioList)
            cacheService.triggerUpdateFlag(CacheKey.AUDIO)
        }
    }

    @DeleteMapping
    suspend fun deleteAudio(authentication: Authentication, @RequestBody request: Request.Id) {
        val audio = audioService.findById(request.id) ?: throw ErrorCode(404, "Audio not found")
        audioService.delete(audio)
        logService.log(LogType.DELETE_AUDIO, authentication.toUser(), "${audio.name} (${audio.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.AUDIO)
    }

    @PutMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestParam audioId: Int, @RequestParam locale: IkusLocale,
                       @RequestPart("file") file: Mono<FilePart>) {
        val audio = audioService.findById(audioId) ?: throw ErrorCode(404, "Audio not found")
        audioService.setImage(audio, file.awaitFirst(), locale)
    }

    @DeleteMapping("/image")
    suspend fun deleteImage(@RequestParam audioId: Int) {
        val audio = audioService.findById(audioId) ?: throw ErrorCode(404, "Audio not found")
        audioService.deleteImages(audio)
    }
}