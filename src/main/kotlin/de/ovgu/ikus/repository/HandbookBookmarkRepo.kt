package de.ovgu.ikus.repository

import de.ovgu.ikus.model.HandbookBookmark
import de.ovgu.ikus.model.IkusLocale
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HandbookBookmarkRepo : CoroutineCrudRepository<HandbookBookmark, Int> {

    suspend fun findByOrderByPage(): List<HandbookBookmark>
    suspend fun findByLocaleOrderByPage(locale: IkusLocale): List<HandbookBookmark>

    @Modifying
    suspend fun deleteByLocale(locale: IkusLocale): Int
}