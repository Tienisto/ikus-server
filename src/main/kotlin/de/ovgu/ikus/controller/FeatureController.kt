package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.FeatureDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.Feature
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.moveDownItem
import de.ovgu.ikus.utils.moveUpItem
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/features")
class FeatureController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val featureService: FeatureService,
        private val postService: PostService,
        private val postFileService: PostFileService,
        private val channelService: ChannelService,
        private val linkService: LinkService
) {

    @GetMapping
    suspend fun getFeatures(): List<FeatureDto> {
        return featureService.findAllOrderByPosition().map { feature ->
            val postDto = when (val postId = feature.postId) {
                null -> null
                else -> when (val post = postService.findById(postId)) {
                    null -> null
                    else -> when (val channel = channelService.findById(post.channelId)?.toDto()) {
                        null -> null
                        else -> post.toDto(channel, postFileService.findByPost(post).map { file -> file.toDto() })
                    }
                }
            }
            val linkDto = when (val linkId = feature.linkId) {
                null -> null
                else -> when (val link = linkService.findById(linkId)) {
                    null -> null
                    else -> when (val channel = channelService.findById(link.channelId)?.toDto()) {
                        null -> null
                        else -> link.toDto(channel)
                    }
                }
            }
            feature.toDto(postDto, linkDto)
        }
    }

    @PostMapping
    suspend fun createFeature(authentication: Authentication, @RequestBody request: Request.CreateFeature) {

        validate(request.postId, request.linkId)

        val maxPosition = featureService.findMaxPosition()
        val feature = Feature(
                name = request.name.en.trim(), nameDe = request.name.de.trim(), icon = request.icon,
                postId = request.postId, linkId = request.linkId,
                favorite = false, position = maxPosition + 1
        )

        featureService.save(feature)
        logService.log(LogType.CREATE_FEATURE, authentication.toUser(), label(feature))
        cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
    }

    @PutMapping
    suspend fun updateFeature(authentication: Authentication, @RequestBody request: Request.UpdateFeature) {
        val feature = featureService.findById(request.id) ?: throw ErrorCode(404, "Feature not found")
        validate(request.postId, request.linkId)

        feature.name = request.name.en.trim()
        feature.nameDe = request.name.de.trim()
        feature.icon = request.icon
        feature.postId = request.postId
        feature.linkId = request.linkId

        featureService.save(feature)
        logService.log(LogType.UPDATE_FEATURE, authentication.toUser(), label(feature))
        cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
    }

    @PostMapping("/toggle-favorite")
    suspend fun toggleFavorite(authentication: Authentication, @RequestParam featureId: Int) {
        val feature = featureService.findById(featureId) ?: throw ErrorCode(404, "Feature not found")

        feature.favorite = !feature.favorite
        featureService.save(feature)
        logService.log(LogType.UPDATE_FEATURE, authentication.toUser(), label(feature))
        cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestParam featureId: Int) {
        val feature = featureService.findById(featureId) ?: throw ErrorCode(404, "Feature not found")
        val features = featureService
                .findAllOrderByPosition()
                .moveUpItem(item = feature, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (features != null) {
            featureService.saveAll(features)
            cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestParam featureId: Int) {
        val feature = featureService.findById(featureId) ?: throw ErrorCode(404, "Feature not found")
        val features = featureService
                .findAllOrderByPosition()
                .moveDownItem(item = feature, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (features != null) {
            featureService.saveAll(features)
            cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
        }
    }

    @DeleteMapping
    suspend fun deleteFeature(authentication: Authentication, @RequestParam featureId: Int) {
        val feature = featureService.findById(featureId) ?: throw ErrorCode(404, "Feature not found")
        if (feature.nativeFeature != null)
            throw ErrorCode(409, "Native Features cannot be deleted")

        featureService.delete(feature)
        logService.log(LogType.DELETE_FEATURE, authentication.toUser(), label(feature))
        cacheService.triggerUpdateFlag(CacheKey.APP_CONFIG)
    }

    private suspend fun validate(postId: Int?, linkId: Int?) {
        if (postId == null && linkId == null)
            throw ErrorCode(400, "Post id or link id missing")

        if (postId != null && linkId != null)
            throw ErrorCode(409, "Only define one, not both post and link")

        // TODO: use existsBy if fixed
        if (postId != null && postService.findById(postId) == null)
            throw ErrorCode(404, "Post not found")

        if (linkId != null && linkService.findById(linkId) == null)
            throw ErrorCode(404, "Link not found")
    }

    private fun label(feature: Feature): String {
        if (feature.nativeFeature != null) {
            return feature.nativeFeature.toString()
        }

        return "${feature.name} (${feature.nameDe})"
    }
}