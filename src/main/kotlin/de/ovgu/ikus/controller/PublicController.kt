package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.LocalizedChannelDto
import de.ovgu.ikus.dto.PublicPostDto
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toLocalizedDto
import org.springframework.core.io.FileSystemResource
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/public")
class PublicController(
        private val cacheService: CacheService,
        private val postService: PostService,
        private val channelService: ChannelService,
        private val fileService: FileService
) {

    @GetMapping("/news")
    suspend fun getPosts(@RequestParam locale: IkusLocale): String {
        return cacheService.getOrUpdate(CacheKey.NEWS, locale) {
            val channels = channelService.findByTypeOrdered(ChannelType.NEWS)
            val channelsDtoMap = channels.map { channel -> channel.id to channel.toLocalizedDto(locale) }.toMap()
            val posts = channels
                    .map { channel -> postService.findByChannelOrdered(channel, 10) }
                    .flatten()
                    .map { post -> post.toLocalizedDto(locale, channelsDtoMap[post.channelId] ?: LocalizedChannelDto(0, "Error")) }
                    .sortedByDescending { post -> post.date }

            PublicPostDto(channelsDtoMap.values.toList(), posts)
        }
    }

    @GetMapping("/file/{folder}/{name}")
    fun getImage(@PathVariable folder: String, @PathVariable name: String, webExchange: ServerWebExchange): FileSystemResource {
        return fileService.loadFile("$folder/$name", webExchange)
    }
}