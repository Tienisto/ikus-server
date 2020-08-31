package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.ChannelService
import de.ovgu.ikus.service.LogService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/channels")
class ChannelController (
        private val logService: LogService,
        private val channelService: ChannelService
) {

    @GetMapping
    suspend fun getAll(): AggregatedChannelDto {
        val channels = channelService.findAll()

        return AggregatedChannelDto(
                post = channels
                        .filter { c -> c.type == ChannelType.POST }
                        .map { channel -> ChannelDto(channel.id, LocalizedString(en = channel.name, de = channel.nameDe)) },
                event = channels
                        .filter { c -> c.type == ChannelType.EVENT }
                        .map { channel -> ChannelDto(channel.id, LocalizedString(en = channel.name, de = channel.nameDe)) }
        )
    }

    @PostMapping
    suspend fun addChannel(authentication: Authentication, @RequestParam type: ChannelType, @RequestBody request: Request.CreateChannel) {
        val channel = channelService.save(Channel(type = type, name = request.name.en.trim(), nameDe = request.name.de.trim()))
        logService.log(LogType.CREATE_CHANNEL, authentication.toUser(), channel.name + " / " + channel.nameDe)
    }

    @PutMapping
    suspend fun renameChannel(authentication: Authentication, @RequestBody request: Request.RenameChannel) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channel.name = request.name.en.trim()
        channel.nameDe = request.name.de.trim()
        channelService.save(channel)
        logService.log(LogType.UPDATE_CHANNEL, authentication.toUser(), channel.name + " / " + channel.nameDe)
    }

    @DeleteMapping
    suspend fun deleteChannel(authentication: Authentication, @RequestBody request: Request.Id) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channelService.delete(channel)
        logService.log(LogType.DELETE_CHANNEL, authentication.toUser(), channel.name + " / " + channel.nameDe)
    }
}