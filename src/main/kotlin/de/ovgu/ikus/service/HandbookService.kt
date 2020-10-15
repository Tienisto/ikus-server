package de.ovgu.ikus.service

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.model.HandbookBookmark
import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.repository.HandbookBookmarkRepo
import kotlinx.coroutines.flow.collect
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class HandbookService (
        private val fileService: FileService,
        private val handbookBookmarkRepo: HandbookBookmarkRepo
) {

    suspend fun uploadFile(file: FilePart, locale: IkusLocale) {
        if (!file.filename().toLowerCase().endsWith(".pdf"))
            throw ErrorCode(409, "PDF only")

        val path = when (locale) {
            IkusLocale.EN -> "handbook/en.pdf"
            IkusLocale.DE -> "handbook/de.pdf"
        }

        fileService.storeFilePart(file, path)
    }

    suspend fun findAllOrdered(): List<HandbookBookmark> {
        return handbookBookmarkRepo.findByOrderByPage()
    }

    suspend fun findByLocaleOrdered(locale: IkusLocale): List<HandbookBookmark> {
        return handbookBookmarkRepo.findByLocaleOrderByPage(locale)
    }

    suspend fun updateBookmarks(bookmarks: List<HandbookBookmark>, locale: IkusLocale) {
        handbookBookmarkRepo.deleteByLocale(locale)
        handbookBookmarkRepo.saveAll(bookmarks).collect()
    }
}