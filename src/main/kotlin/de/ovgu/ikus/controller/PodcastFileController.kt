package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.PodcastFileDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.PodcastFile
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
@RequestMapping("/api/podcasts/file")
class PodcastFileController (
    private val logService: LogService,
    private val cacheService: CacheService,
    private val podcastService: PodcastService,
    private val podcastFileService: PodcastFileService
) {

    @PostMapping
    suspend fun createPodcastFile(authentication: Authentication, @RequestBody request: Request.CreatePodcastFile): PodcastFileDto {
        val podcast = podcastService.findById(request.podcastId) ?: throw ErrorCode(404, "Podcast not found")
        val maxPosition = podcastFileService.findMaxPosition()
        val file = PodcastFile(
            podcastId = podcast.id,
            name = request.name.en.trim(), nameDe = request.name.de.trim(),
            text = request.text?.en?.trimOrNull(), textDe = request.text?.de?.trimOrNull(),
            position = maxPosition + 1
        )

        val savedFile = podcastFileService.save(file)
        logService.log(LogType.CREATE_PODCAST_FILE, authentication.toUser(), "${podcast.name} / ${file.name}")
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)

        return savedFile.toDto()
    }

    @PutMapping
    suspend fun updatePodcastFile(authentication: Authentication, @RequestBody request: Request.UpdatePodcastFile) {
        val file = podcastFileService.findById(request.id) ?: throw ErrorCode(404, "Podcast File not found")
        val podcast = podcastService.findById(request.podcastId) ?: throw ErrorCode(404, "Podcast not found")

        // apply
        file.podcastId = podcast.id
        file.name = request.name.en.trim()
        file.nameDe = request.name.de.trim()
        file.text = request.text?.en?.trimOrNull()
        file.textDe = request.text?.de?.trimOrNull()

        podcastFileService.save(file)
        logService.log(LogType.UPDATE_PODCAST_FILE, authentication.toUser(), "${podcast.name} / ${file.name}")
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = podcastFileService.findById(request.id) ?: throw ErrorCode(404, "Podcast File not found")
        val files = podcastFileService
            .findAllOrdered()
            .moveUpItem(item = file, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (files != null) {
            podcastFileService.saveAll(files)
            cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = podcastFileService.findById(request.id) ?: throw ErrorCode(404, "Podcast File not found")
        val files = podcastFileService
            .findAllOrdered()
            .moveDownItem(item = file, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (files != null) {
            podcastFileService.saveAll(files)
            cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
        }
    }

    @DeleteMapping
    suspend fun deletePodcastFile(authentication: Authentication, @RequestBody request: Request.Id) {
        val file = podcastFileService.findById(request.id) ?: throw ErrorCode(404, "Podcast File not found")
        podcastFileService.delete(file)
        logService.log(LogType.DELETE_PODCAST_FILE, authentication.toUser(), file.name)
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
    }

    @PutMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestParam fileId: Int, @RequestParam locale: IkusLocale,
                       @RequestPart("file") file: Mono<FilePart>) {
        val audioFile = podcastFileService.findById(fileId) ?: throw ErrorCode(404, "Podcast File not found")
        podcastFileService.setAudio(audioFile, file.awaitFirst(), locale)
    }
}