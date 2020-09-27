package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.ChannelType
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.PostType
import de.ovgu.ikus.security.JwtService
import de.ovgu.ikus.service.*
import de.ovgu.ikus.utils.toDto
import de.ovgu.ikus.utils.toLocalizedDto
import org.springframework.core.io.FileSystemResource
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange

@RestController
@RequestMapping("/api/public")
class PublicController(
        private val cacheService: CacheService,
        private val jwtService: JwtService,
        private val postService: PostService,
        private val postFileService: PostFileService,
        private val eventService: EventService,
        private val channelService: ChannelService,
        private val fileService: FileService,
        private val linkService: LinkService,
        private val handbookService: HandbookService,
        private val contactService: ContactService,
        private val analyticsService: AnalyticsService,
        private val mensaService: MensaService
) {

    @GetMapping("/file/{folder}/{name}")
    fun getImage(@PathVariable folder: String, @PathVariable name: String, @RequestParam(required = false) download: Boolean?, webExchange: ServerWebExchange): FileSystemResource {
        return fileService.loadFile("$folder/$name", download == true, webExchange)
    }

    @GetMapping("/news")
    suspend fun getPosts(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.NEWS, locale) {
            val channels = channelService.findByTypeOrderByName(ChannelType.NEWS, locale)
            val channelsDtoMap = channels.map { channel -> channel.id to channel.toLocalizedDto(locale) }.toMap()
            val postsUnsorted = channels
                    .map { channel -> postService.findByChannelOrderByDate(channel, 10) }
                    .flatten()
            val files = postFileService.findByPostIn(postsUnsorted)
            val posts = postsUnsorted
                    .map { post ->
                        val currFiles = files
                                .filter { file -> file.postId == post.id }
                                .map { file -> file.toDto() }
                        post.toLocalizedDto(locale, channelsDtoMap[post.channelId] ?: LocalizedChannelDto(0, "Error"), currFiles)
                    }
                    .sortedByDescending { post -> post.date }

            PublicPostDto(channelsDtoMap.values.toList(), posts)
        }
    }

    @GetMapping("/calendar")
    suspend fun getEvents(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.CALENDAR, locale) {
            val channels = channelService.findByTypeOrderByName(ChannelType.CALENDAR, locale)
            val channelsDtoMap = channels.map { channel -> channel.id to channel.toLocalizedDto(locale) }.toMap()
            val events = eventService
                    .findAllOrdered()
                    .map { event ->
                        val channel = channelsDtoMap[event.channelId] ?: LocalizedChannelDto(0, "Error")
                        event.toLocalizedDto(locale, channel)
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
                        menuInfo.toLocalizedDto(menus)
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

    @GetMapping("/faq")
    suspend fun getFAQ(@RequestParam locale: IkusLocale): String {
        return cacheService.getCacheOrUpdate(CacheKey.FAQ, locale) {
            val channels = channelService.findByTypeOrderByPosition(ChannelType.FAQ)
            val posts = postService.findByTypeOrderByPosition(PostType.FAQ)
            val files = postFileService.findByPostIn(posts)
            channels.map { channel ->
                val channelDto = channel.toLocalizedDto(locale)
                val postsDto = posts
                        .filter { post -> post.channelId == channel.id}
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

    @PostMapping("/start")
    suspend fun appStart(@RequestBody(required = false) request: Request.AppStartSignal?) {
        if (request == null)
            return

        if (!jwtService.checkAppToken(request.token))
            return

        if (request.platform == null || request.deviceId == null)
            return

        analyticsService.handleAppStart(request.platform, request.deviceId)
    }
}