package de.ovgu.ikus.controller

import de.ovgu.ikus.BuildInfo
import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.security.JwtService
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/api")
class InfoController (
        private val propsAdmin: AdminProperties,
        private val logService: LogService,
        private val postService: PostService,
        private val postFileService: PostFileService,
        private val channelService: ChannelService,
        private val fileService: FileService,
        private val jwtService: JwtService
) {

    private var startTime: Long = 0

    @PostConstruct
    fun init() {
        startTime = System.currentTimeMillis()
    }

    @GetMapping("/version")
    fun version(): VersionDto {
        return VersionDto(BuildInfo.VERSION)
    }

    @GetMapping("/status")
    fun status(): StatusDto {
        return StatusDto(
                version = BuildInfo.VERSION,
                date = BuildInfo.BUILD_DATE,
                runTime = System.currentTimeMillis() - startTime,
                database = true,
                storageRead = fileService.hasReadAccess(),
                storageWrite = fileService.hasWriteAccess(),
                adminPassword = propsAdmin.password != BuildInfo.DEFAULT_PROPS["admin.password"],
                jwtWebsite = jwtService.getStatusWebsite(),
                jwtApp = jwtService.getStatusApp()
        )
    }

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
        val channels = channelService.findByTypeOrdered(ChannelType.NEWS)
        val posts = postService.findAllOrdered(5).map { post ->
            val channel = channels
                    .first { c -> c.id == post.channelId }
                    .toDto()

            val files = postFileService
                    .findByPost(post)
                    .map { file -> file.toDto() }
            post.toDto(channel, files)
        }
        return DashboardDto(logs, posts)
    }
}