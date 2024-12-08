package de.ovgu.ikus.controller

import de.ovgu.ikus.BuildInfo
import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import de.ovgu.ikus.security.CryptoUtils
import de.ovgu.ikus.security.JwtService
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/api/public")
class PublicController(
    private val cryptoUtils: CryptoUtils,
    private val cacheService: CacheService,
    private val jwtService: JwtService,
    private val postService: PostService,
    private val postFileService: PostFileService,
    private val eventService: EventService,
    private val channelService: ChannelService,
    private val fileService: FileService,
    private val linkService: LinkService,
    private val handbookService: HandbookService,
    private val audioService: AudioService,
    private val audioFileService: AudioFileService,
    private val contactService: ContactService,
    private val featureService: FeatureService,
    private val analyticsService: AnalyticsService,
    private val mensaService: MensaService
) {

    private val logger = LoggerFactory.getLogger(PublicController::class.java)
    private val newsComparator = compareByDescending<LocalizedPostDto> { it.pinned }.thenByDescending { it.date }

    @GetMapping("/file/{folder}/{name}")
    fun getFile(@PathVariable folder: String, @PathVariable name: String, @RequestParam(required = false) download: Boolean?, webExchange: ServerWebExchange): FileSystemResource? {
        return fileService.loadFile("$folder/$name", download == true, webExchange)
    }

    @GetMapping("/file/{folder}/{subFolder}/{name}")
    fun getFileSubFolder(@PathVariable folder: String, @PathVariable subFolder: String, @PathVariable name: String, @RequestParam(required = false) download: Boolean?, webExchange: ServerWebExchange): FileSystemResource? {
        return fileService.loadFile("$folder/$subFolder/$name", download == true, webExchange)
    }

    @GetMapping("/news")
    suspend fun getNews(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.NEWS, locale) {
            val channels = channelService.findByTypeOrderByName(ChannelType.NEWS, locale)
            val channelsDtoMap = channels.associate { channel -> channel.id to channel.toLocalizedDto(locale) }
            val postsUnsorted = channels
                .map { channel -> postService.findByChannelIdOrderByPositionLimited(channel.id, 10) }
                .flatten()
            val files = postFileService.findByPostIn(postsUnsorted)
            val posts = postsUnsorted
                .map { post ->
                    val currFiles = files
                        .filter { file -> file.postId == post.id }
                        .map { file -> file.toDto() }
                    post.toLocalizedDto(locale, channelsDtoMap[post.channelId] ?: LocalizedChannelDto(0, "Error"), currFiles)
                }
                .sortedWith(newsComparator)

            PublicPostDto(channelsDtoMap.values.toList(), posts)
        }
    }

    @GetMapping("/calendar")
    suspend fun getEvents(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.CALENDAR, locale) {
            val channels = channelService.findByTypeOrderByName(ChannelType.CALENDAR, locale)
            val channelsDtoMap = channels.associate { channel -> channel.id to channel.toLocalizedDto(locale) }
            val events = eventService
                .findAllOrdered()
                .map { event ->
                    val channel = channelsDtoMap[event.channelId] ?: LocalizedChannelDto(0, "Error")
                    event.toLocalizedDto(locale, channel, cryptoUtils)
                }

            PublicEventDto(channelsDtoMap.values.toList(), events)
        }
    }

    @GetMapping("/mensa")
    suspend fun getMensa(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.MENSA, locale) {
            mensaService
                .getMenu()
                .map { menuInfo ->
                    val menus = menuInfo.menus.map { menu ->
                        val food = menu.food.map { food -> food.toLocalizedDto(locale) }
                        menu.toLocalizedDto(food)
                    }
                    menuInfo.toLocalizedDto(locale, menus)
                }
        }
    }

    @GetMapping("/links")
    suspend fun getLinks(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.LINKS, locale) {
            val channels = channelService.findByTypeOrderByPosition(ChannelType.LINK)
            val links = linkService.findAllOrdered()
            channels.map { channel ->
                val groupDto = channel.toLocalizedDto(locale)
                val linksDto = links
                    .filter { link -> link.channelId == channel.id }
                    .map { link -> link.toLocalizedDto(locale, groupDto) }

                PublicLinkDto(groupDto, linksDto)
            }
        }
    }

    @GetMapping("/handbook-bookmarks")
    suspend fun getHandbookBookmarks(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.HANDBOOK_BOOKMARKS, locale) {
            handbookService
                .findByLocaleOrdered(locale)
                .map { bookmark -> bookmark.toDto() }
        }
    }

    @GetMapping("/audio")
    suspend fun getAudio(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.AUDIO, locale) {
            val files = audioFileService.findAllOrdered()
            audioService
                .findAllOrdered()
                .map { audio ->
                    val currFiles = files
                        .filter { file -> file.audioId == audio.id }
                        .map { file -> file.toLocalizedDto(locale) }

                    audio.toLocalizedDto(locale, currFiles)
                }
        }
    }

    @GetMapping("/faq")
    suspend fun getFAQ(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.FAQ, locale) {
            val channels = channelService.findByTypeOrderByPosition(ChannelType.FAQ)
            val posts = postService.findByTypeOrderByPosition(PostType.FAQ)
            val files = postFileService.findByPostIn(posts)
            channels.map { channel ->
                val channelDto = channel.toLocalizedDto(locale)
                val postsDto = posts
                    .filter { post -> post.channelId == channel.id }
                    .map { post ->
                        val currFiles = files
                            .filter { file -> file.postId == post.id }
                            .map { file -> file.toDto() }
                        post.toLocalizedDto(locale, channelDto, currFiles)
                    }
                PublicFAQDto(channelDto, postsDto)
            }
        }
    }

    @GetMapping("/contacts")
    suspend fun getContacts(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.CONTACTS, locale) {
            contactService
                .findAllOrdered()
                .map { contact -> contact.toLocalizedDto(locale) }
        }
    }

    @GetMapping("/app-config")
    suspend fun getAppConfig(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.APP_CONFIG, locale) {
            val features = featureService
                .findAllOrderByPosition()
                .map { feature ->
                    val postDto = when (val postId = feature.postId) {
                        null -> null
                        else -> when (val post = postService.findById(postId)) {
                            null -> null
                            else -> when (val channel = channelService.findById(post.channelId)?.toLocalizedDto(locale)) {
                                null -> null
                                else -> post.toLocalizedDto(locale, channel, postFileService.findByPost(post).map { file -> file.toDto() })
                            }
                        }
                    }
                    val linkDto = when (val linkId = feature.linkId) {
                        null -> null
                        else -> when (val link = linkService.findById(linkId)) {
                            null -> null
                            else -> when (val channel = channelService.findById(link.channelId)?.toLocalizedDto(locale)) {
                                null -> null
                                else -> link.toLocalizedDto(locale, channel)
                            }
                        }
                    }
                    feature.toLocalizedDto(locale, postDto, linkDto)
                }

            PublicConfigDto(
                version = BuildInfo.API_LEVEL,
                features = features
            )
        }
    }

    @GetMapping("/combined", "/batch")
    suspend fun getBatch(@RequestParam locale: IkusLocale, @RequestParam routes: List<CacheKey>): Map<String, String> {
        return routes.associate { route ->
            val value = when (route) {
                CacheKey.NEWS -> getNews(locale)
                CacheKey.CALENDAR -> getEvents(locale)
                CacheKey.MENSA -> getMensa(locale)
                CacheKey.LINKS -> getLinks(locale)
                CacheKey.HANDBOOK_BOOKMARKS -> getHandbookBookmarks(locale)
                CacheKey.AUDIO -> getAudio(locale)
                CacheKey.FAQ -> getFAQ(locale)
                CacheKey.CONTACTS -> getContacts(locale)
                CacheKey.APP_CONFIG -> getAppConfig(locale)
            }

            route.name to value
        }
    }

    @PostMapping("/start")
    suspend fun appStart(@RequestBody(required = false) request: Request.AppStartSignal?) {
        if (request == null)
            return

        if (!jwtService.checkAppToken(request.token))
            return

        if (request.platform == null || request.deviceId == null)
            return

        analyticsService.saveAppStartCache(request.platform, request.deviceId)
    }

    @PostMapping("/event/register")
    suspend fun registerEvent(@RequestBody payload: Request.RegisterEvent): RegisterEventResponse? {
        if (!jwtService.checkAppToken(payload.jwt)) {
            logger.info("Wrong JWT (calling /api/public/event/register)")
            return null
        }

        val event = eventService.findById(payload.eventId) ?: throw ErrorCode(404, "Event not found")

        if (!event.registrationOpen)
            throw ErrorCode(403, "Closed")

        if (event.registrations.size >= event.registrationSlots + event.registrationSlotsWaiting)
            throw ErrorCode(409, "Full")

        // input validation
        event.registrationFields.forEach { field ->
            when (RegistrationField.valueOf(field)) {
                RegistrationField.MATRICULATION_NUMBER -> if (payload.matriculationNumber == null) throw ErrorCode(400, "Missing matriculation number")
                RegistrationField.FIRST_NAME -> if (payload.firstName.isNullOrBlank()) throw ErrorCode(400, "Missing first name")
                RegistrationField.LAST_NAME -> if (payload.lastName.isNullOrBlank()) throw ErrorCode(400, "Missing last name")
                RegistrationField.EMAIL -> if (payload.email.isNullOrBlank()) throw ErrorCode(400, "Missing email")
                RegistrationField.ADDRESS -> if (payload.address.isNullOrBlank()) throw ErrorCode(400, "Missing address")
                RegistrationField.COUNTRY -> if (payload.country.isNullOrBlank()) throw ErrorCode(400, "Missing country")
                RegistrationField.DATE_OF_BIRTH -> if (payload.dateOfBirth == null) throw ErrorCode(400, "Missing date of birth")
            }
        }

        val registrationData = RegistrationData(
            timestamp = OffsetDateTime.now().germany(),
            token = UUID.randomUUID().toString(),
            matriculationNumber = payload.matriculationNumber,
            firstName = payload.firstName,
            lastName = payload.lastName,
            email = payload.email,
            address = payload.address,
            country = payload.country,
            dateOfBirth = payload.dateOfBirth,
        )

        event.registrations += registrationData.toJSON()
        eventService.save(event)
        cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
        return RegisterEventResponse(registrationData.token)
    }

    @PostMapping("/event/unregister")
    suspend fun unregisterEvent(@RequestBody payload: Request.UnregisterEvent) {
        if (!jwtService.checkAppToken(payload.jwt))
            return

        val event = eventService.findById(payload.eventId) ?: return
        val changed = eventService.removeRegisteredUser(event, payload.token)

        if (changed) {
            cacheService.triggerUpdateFlag(CacheKey.CALENDAR)
        } else {
            logger.info("Unregister failed (invalid token: ${payload.token})")
        }
    }
}