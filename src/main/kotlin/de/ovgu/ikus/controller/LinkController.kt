package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.moveDownItem
import de.ovgu.ikus.utils.moveUpItem
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/links")
class LinkController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val linkService: LinkService,
        private val channelService: ChannelService
) {

    @GetMapping
    suspend fun getAll(): List<LinkGroupDto> {
        val channels = channelService.findByTypeOrderByPosition(ChannelType.LINK)
        val links = linkService.findAllOrdered()

        return channels.map { channel ->
            val channelDto = channel.toDto()
            val currLinks = links
                    .filter { link -> link.channelId == channel.id }
                    .map { link -> link.toDto(channelDto) }

            LinkGroupDto(channelDto, currLinks)
        }
    }

    @GetMapping("/search")
    suspend fun getBySearch(@RequestParam query: String): List<LinkDto> {
        val channels = channelService.findByType(ChannelType.LINK)
        val channelsDtoMap = channels.map { channel -> channel.id to channel.toDto() }.toMap()
        val links = linkService.search(query)

        return links.map { link ->
            val channel = channelsDtoMap[link.channelId] ?: ChannelDto(0, MultiLocaleString("ERROR", "ERROR"))
            link.toDto(channel)
        }
    }

    @PostMapping
    suspend fun createLink(authentication: Authentication, @RequestBody request: Request.CreateLink) {
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        if (channel.type != ChannelType.LINK)
            throw ErrorCode(409, "Wrong Channel Type")

        val maxPosition = linkService.findMaxPositionByChannel(channel)
        val link = Link(channelId = channel.id,
                        url = request.url.en.trim(), urlDe = request.url.de.trim(),
                        info = request.info.en.trim(), infoDe = request.info.de.trim(),
                        position = maxPosition + 1)

        linkService.save(link)
        logService.log(LogType.CREATE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @PutMapping
    suspend fun updateLink(authentication: Authentication, @RequestBody request: Request.UpdateLink) {
        val channel = channelService.findById(request.channelId) ?: throw ErrorCode(404, "Channel not found")
        if (channel.type != ChannelType.LINK)
            throw ErrorCode(409, "Wrong Channel Type")

        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")

        link.channelId = channel.id
        link.url = request.url.en.trim()
        link.urlDe = request.url.de.trim()
        link.info = request.info.en.trim()
        link.infoDe = request.info.de.trim()

        linkService.save(link)
        logService.log(LogType.UPDATE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }

    @PostMapping("/move-up")
    suspend fun moveUp(authentication: Authentication, @RequestBody request: Request.Id) {
        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")
        val links = linkService
                .findByChannelId(link.channelId)
                .moveUpItem(item = link, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (links != null) {
            linkService.saveAll(links)
            cacheService.triggerUpdateFlag(CacheKey.LINKS)
        }
    }

    @PostMapping("/move-down")
    suspend fun moveDown(authentication: Authentication, @RequestBody request: Request.Id) {
        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")
        val links = linkService
                .findByChannelId(link.channelId)
                .moveDownItem(item = link, equals = { a, b -> a.id == b.id }, setIndex = { item, index -> item.position = index })

        if (links != null) {
            linkService.saveAll(links)
            cacheService.triggerUpdateFlag(CacheKey.LINKS)
        }
    }

    @DeleteMapping
    suspend fun deleteLink(authentication: Authentication, @RequestBody request: Request.Id) {
        val link = linkService.findById(request.id) ?: throw ErrorCode(404, "Link not found")
        linkService.delete(link)
        logService.log(LogType.DELETE_LINK, authentication.toUser(), "${link.info} (${link.url})")
        cacheService.triggerUpdateFlag(CacheKey.LINKS)
    }
}