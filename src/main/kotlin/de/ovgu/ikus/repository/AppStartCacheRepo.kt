package de.ovgu.ikus.repository

import de.ovgu.ikus.model.AppStartCache
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.OffsetDateTime

interface AppStartCacheRepo : CoroutineCrudRepository<AppStartCache, String> {

    fun findByLastUpdateAfter(timestamp: OffsetDateTime): Flow<AppStartCache>

    @Modifying
    @Query("DELETE FROM app_start_cache WHERE last_update < :timestamp")
    suspend fun deleteOlderThan(timestamp: OffsetDateTime): Int
}