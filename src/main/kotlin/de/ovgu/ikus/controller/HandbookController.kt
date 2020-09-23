package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.HandbookBookmarkGroupedDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.HandbookBookmark
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.CacheKey
import de.ovgu.ikus.service.CacheService
import de.ovgu.ikus.service.HandbookService
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.utils.toDto
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/handbook")
class HandbookController (
        private val logService: LogService,
        private val cacheService: CacheService,
        private val handbookService: HandbookService
) {

    @GetMapping
    suspend fun getBookmarks(): List<HandbookBookmarkGroupedDto> {
        val bookmarks = handbookService.findAllOrdered()
        return IkusLocale
                .values()
                .map { locale ->
                    val currBookmarks = bookmarks
                            .filter { bookmark -> bookmark.locale == locale }
                            .map { bookmark -> bookmark.toDto() }

                    HandbookBookmarkGroupedDto(locale, currBookmarks)
                }
    }

    @PutMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun upload(authentication: Authentication, @RequestPart("file") file: Mono<FilePart>, @RequestParam locale: IkusLocale) {
        handbookService.uploadFile(file.awaitFirst(), locale)
        logService.log(LogType.UPDATE_HANDBOOK, authentication.toUser(), locale.toString())
    }

    @PutMapping("/bookmarks")
    suspend fun updateBookmarks(authentication: Authentication, @RequestBody request: Request.UpdateBookmarks) {
        val locale = request.locale
        val bookmarks = request.bookmarks.map { bookmark -> HandbookBookmark(locale = locale, page = bookmark.page, name = bookmark.name) }
        handbookService.updateBookmarks(bookmarks, locale)
        logService.log(LogType.UPDATE_HANDBOOK_BOOKMARKS, authentication.toUser(), locale.toString())
        cacheService.triggerUpdateFlag(CacheKey.HANDBOOK_BOOKMARKS)
    }
}