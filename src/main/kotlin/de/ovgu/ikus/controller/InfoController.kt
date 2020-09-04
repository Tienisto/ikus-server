package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.DashboardDto
import de.ovgu.ikus.dto.LogDto
import de.ovgu.ikus.dto.MeDto
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.ChannelService
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.service.PostService
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class InfoController (
        private val logService: LogService,
        private val postService: PostService,
        private val channelService: ChannelService
) {

    @GetMapping("/me")
    fun me(authentication: Authentication): MeDto {
        val user = authentication.toUser()
        val admin = authentication.isAdmin()
        return MeDto(user.name, admin)
    }

    @GetMapping("/logs")
    suspend fun logs(@RequestParam limit: Int): List<LogDto> {
        return logService.findAll(limit)
    }

    @GetMapping("/dashboard")
    suspend fun dashboard(): DashboardDto {
        val logs = logService.findAll(5)
        val channels = channelService.findByType(ChannelType.NEWS)
        val posts = postService.findAllOrdered(5).map { post ->
            val channel = channels
                    .first { c -> c.id == post.channelId }
                    .toDto()
            post.toDto(channel)
        }
        return DashboardDto(logs, posts)
    }
}