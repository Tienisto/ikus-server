package de.ovgu.ikus.repository

import de.ovgu.ikus.model.HandbookBookmark
import de.ovgu.ikus.model.IkusLocale
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HandbookBookmarkRepo : CoroutineCrudRepository<HandbookBookmark, Int> {

    fun findByOrderByPage(): Flow<HandbookBookmark>
    fun findByLocaleOrderByPage(locale: IkusLocale): Flow<HandbookBookmark>

    @Modifying
    suspend fun deleteByLocale(locale: IkusLocale): Int
}