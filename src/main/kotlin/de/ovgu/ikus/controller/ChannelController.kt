package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ChannelDto
import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.LocalizedString
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.Channel
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.service.ChannelService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/channels")
class ChannelController (
        private val channelService: ChannelService
) {

    @GetMapping
    suspend fun getAll(@RequestParam type: ChannelType): List<ChannelDto> {
        return channelService
                .findByType(type)
                .map { channel -> ChannelDto(channel.id, LocalizedString(en = channel.name, de = channel.nameDe)) }
    }

    @PostMapping
    suspend fun addChannel(authentication: Authentication, @RequestParam type: ChannelType, @RequestBody request: Request.CreateChannel) {
        channelService.save(Channel(type = type, name = request.name.en.trim(), nameDe = request.name.de.trim()))
    }

    @PutMapping
    suspend fun renameChannel(authentication: Authentication, @RequestParam type: ChannelType, @RequestBody request: Request.RenameChannel) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channel.name = request.name.en.trim()
        channel.nameDe = request.name.de.trim()
        channelService.save(channel)
    }

    @DeleteMapping
    suspend fun deleteChannel(authentication: Authentication, @RequestParam type: ChannelType, @RequestBody request: Request.Id) {
        val channel = channelService.findById(request.id) ?: throw ErrorCode(404, "Channel not found")
        channelService.delete(channel)
    }
}