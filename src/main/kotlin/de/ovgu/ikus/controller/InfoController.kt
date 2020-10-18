package de.ovgu.ikus.controller

import de.ovgu.ikus.BuildInfo
import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.PostType
import de.ovgu.ikus.properties.*
import de.ovgu.ikus.security.JwtService
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toDto
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/api")
class InfoController (
        private val propsDB: DBProperties,
        private val propsAdmin: AdminProperties,
        private val propsJwt: JwtProperties,
        private val propsStorage: StorageProperties,
        private val propsRoutes: RoutesProperties,
        private val logService: LogService,
        private val postService: PostService,
        private val postFileService: PostFileService,
        private val channelService: ChannelService,
        private val fileService: FileService,
        private val jwtService: JwtService,
        private val eventService: EventService
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
    suspend fun status(request: ServerHttpRequest): StatusDto {

        val token = request.cookies["jwt"]?.firstOrNull()?.value
        val env = when (jwtService.getUser(token)?.name) {
            propsAdmin.name -> {
                listOf(
                        EnvDto("storage.path", "Pfad zum persistenten Dateispeicher", "(ein invalider Pfad)", propsStorage.path),
                        EnvDto("db.url", "URL der Datenbank", BuildInfo.DEFAULT_PROPS["db.url"] as String, propsDB.url),
                        EnvDto("db.user", "Datenbank-Nutzer", BuildInfo.DEFAULT_PROPS["db.user"] as String, propsDB.user),
                        EnvDto("db.password", "Datenbank-Passwort", BuildInfo.DEFAULT_PROPS["db.password"] as String, "*********"),
                        EnvDto("admin.password", "Passwort des Admin-Accounts", BuildInfo.DEFAULT_PROPS["admin.password"] as String, "*********"),
                        EnvDto("jwt.website", "Privatschlüssel für die Webseite", BuildInfo.DEFAULT_PROPS["jwt.website"] as String, "*********"),
                        EnvDto("jwt.app", "Privatschlüssel für die App", BuildInfo.DEFAULT_PROPS["jwt.app"] as String, "*********"),
                        EnvDto("jwt.timeout", "Lebensdauer des JWT in Millisekunden", BuildInfo.DEFAULT_PROPS["jwt.timeout"] as String, propsJwt.timeout.toString()),
                        EnvDto("routes.dev", "Dev API erlauben", BuildInfo.DEFAULT_PROPS["routes.dev"] as String, propsRoutes.dev.toString())
                )
            }
            else -> null
        }

        return StatusDto(
                version = BuildInfo.VERSION,
                date = BuildInfo.BUILD_DATE,
                runTime = System.currentTimeMillis() - startTime,
                database = true,
                storageRead = fileService.hasReadAccess(),
                storageWrite = fileService.hasWriteAccess(),
                adminPassword = propsAdmin.password != BuildInfo.DEFAULT_PROPS["admin.password"],
                jwtWebsite = jwtService.getStatusWebsite(),
                jwtApp = jwtService.getStatusApp(),
                env = env
        )
    }

    @GetMapping("/logs")
    suspend fun getUserLogs(@RequestParam limit: Int): List<LogDto> {
        return logService.findAll(limit)
    }

    @GetMapping("/sys-logs")
    suspend fun getSystemLogs(): SysLogsDto {
        val logs = fileService.loadFileAsString("logs/spring.log")
        return SysLogsDto(logs)
    }

    @GetMapping("/dashboard")
    suspend fun dashboard(): DashboardDto {
        val logs = logService.findAll(7)
        val postChannelMap = channelService
                .findByType(ChannelType.NEWS)
                .map { channel -> channel.id to channel.toDto() }
                .toMap()
        val posts = postService.findByTypeOrderByDateLimited(PostType.NEWS, 6).map { post ->
            val channel = postChannelMap[post.channelId] ?: ChannelDto(0, MultiLocaleString("Error", "Error"))

            val files = postFileService
                    .findByPost(post)
                    .map { file -> file.toDto() }
            post.toDto(channel, files)
        }

        val eventChannelMap = channelService
                .findByType(ChannelType.CALENDAR)
                .map { channel -> channel.id to channel.toDto() }
                .toMap()

        val events = eventService.findAllOrdered().map { event ->
            val channel = eventChannelMap[event.channelId] ?: ChannelDto(0, MultiLocaleString("Error", "Error"))
            event.toDto(channel)
        }

        return DashboardDto(logs, posts, events)
    }
}