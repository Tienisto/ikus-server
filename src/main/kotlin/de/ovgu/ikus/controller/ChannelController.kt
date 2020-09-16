package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.CacheKey
import de.ovgu.ikus.service.CacheService
import de.ovgu.ikus.service.ChannelService
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/channels")
class ChannelController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val channelService: ChannelService
) {

    @GetMapping
    suspend fun getAll(@RequestParam(required = false) type: ChannelType?): Any {
        if (type != null) {
            return channelService.findByTypeOrdered(type, IkusLocale.EN).map { channel -> channel.toDto() }
        } else {
            val channels = channelService.findAll()

            return AllChannelDto(
                    post = channels
                            .filter { c -> c.type == ChannelType.NEWS }
                            .map { channel -> channel.toDto() },
                    event = channels
                            .filter { c -> c.type == ChannelType.CALENDAR }
                            .map { channel -> channel.toDto() }
            )
        }
    }

    @PostMapping
    suspend fun createChannel(authentication: Authentication, @RequestParam type: ChannelType, @RequestBody request: Request.CreateChannel) {
        val channel = channelService.save(Channel(type = type, name = request.name.en.trim(), nameDe = request.name.de.trim()))
        logService.log(LogType.CREATE_CHANNEL, authentication.toUser(), "${channel.name} (${channel.nameDe})")
    }

    @PutMapping
    suspend fun renameChannel(authentication: Authentication, @RequestBody request: Request.RenameChannel) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channel.name = request.name.en.trim()
        channel.nameDe = request.name.de.trim()
        channelService.save(channel)
        logService.log(LogType.UPDATE_CHANNEL, authentication.toUser(), "${channel.name} (${channel.nameDe})")

        when (channel.type) {
            ChannelType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            ChannelType.CALENDAR -> {}
            ChannelType.FAQ -> {}
        }
    }

    @DeleteMapping
    suspend fun deleteChannel(authentication: Authentication, @RequestBody request: Request.Id) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channelService.delete(channel)
        logService.log(LogType.DELETE_CHANNEL, authentication.toUser(), "${channel.name} (${channel.nameDe})")

        when (channel.type) {
            ChannelType.NEWS -> cacheService.triggerUpdateFlag(CacheKey.NEWS)
            ChannelType.CALENDAR -> {}
            ChannelType.FAQ -> {}
        }
    }
}