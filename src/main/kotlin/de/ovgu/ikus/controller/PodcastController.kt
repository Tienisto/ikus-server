package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.PodcastDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.Podcast
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
@RequestMapping("/api/podcasts")
class PodcastController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val podcastService: PodcastService,
        private val podcastFileService: PodcastFileService
) {

    @GetMapping
    suspend fun getPodcasts(): List<PodcastDto> {
        val files = podcastFileService.findAllOrdered()
        return podcastService
            .findAllOrdered()
            .map { podcast ->
                val currFiles = files
                    .filter { file -> file.podcastId == podcast.id }
                    .map { file -> file.toDto() }

                podcast.toDto(currFiles)
            }
    }

    @PostMapping
    suspend fun createPodcast(authentication: Authentication, @RequestBody request: Request.CreatePodcast): PodcastDto {
        val maxPosition = podcastService.findMaxPosition()
        val podcast = Podcast(
            name = request.name.en.trim(), nameDe = request.name.de.trim(),
            position = maxPosition + 1
        )

        val savedPodcast = podcastService.save(podcast)
        logService.log(LogType.CREATE_PODCAST, authentication.toUser(), "${podcast.name} (${podcast.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)

        return savedPodcast.toDto(emptyList())
    }

    @PutMapping
    suspend fun updatePodcast(authentication: Authentication, @RequestBody request: Request.UpdatePodcast) {
        val podcast = podcastService.findById(request.id) ?: throw ErrorCode(404, "Podcast not found")

        // apply
        podcast.name = request.name.en.trim()
        podcast.nameDe = request.name.de.trim()

        podcastService.save(podcast)
        logService.log(LogType.UPDATE_PODCAST, authentication.toUser(), "${podcast.name} (${podcast.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val podcast = podcastService.findById(request.id) ?: throw ErrorCode(404, "Podcast not found")
        val podcasts = podcastService
                .findAllOrdered()
                .moveUpItem(item = podcast, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (podcasts != null) {
            podcastService.saveAll(podcasts)
            cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val podcast = podcastService.findById(request.id) ?: throw ErrorCode(404, "Podcast not found")
        val podcasts = podcastService
            .findAllOrdered()
            .moveDownItem(item = podcast, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (podcasts != null) {
            podcastService.saveAll(podcasts)
            cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
        }
    }

    @DeleteMapping
    suspend fun deletePodcast(authentication: Authentication, @RequestBody request: Request.Id) {
        val podcast = podcastService.findById(request.id) ?: throw ErrorCode(404, "Podcast not found")
        podcastService.delete(podcast)
        logService.log(LogType.DELETE_PODCAST, authentication.toUser(), "${podcast.name} (${podcast.nameDe})")
        cacheService.triggerUpdateFlag(CacheKey.PODCASTS)
    }

    @PutMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(@RequestParam podcastId: Int, @RequestParam locale: IkusLocale,
                       @RequestPart("file") file: Mono<FilePart>) {
        val podcast = podcastService.findById(podcastId) ?: throw ErrorCode(404, "Podcast not found")
        podcastService.setImage(podcast, file.awaitFirst(), locale)
    }

    @DeleteMapping("/image")
    suspend fun deleteFile(@RequestParam podcastId: Int) {
        val podcast = podcastService.findById(podcastId) ?: throw ErrorCode(404, "Podcast not found")
        podcastService.deleteImages(podcast)
    }
}