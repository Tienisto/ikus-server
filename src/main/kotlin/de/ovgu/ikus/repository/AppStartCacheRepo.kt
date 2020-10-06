package de.ovgu.ikus.repository

import de.ovgu.ikus.dto.CurrentAppStarts
import de.ovgu.ikus.model.AppStartCache
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitFirst
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

interface AppStartCacheRepo : CoroutineCrudRepository<AppStartCache, String> {

    fun findByLastUpdateAfter(timestamp: OffsetDateTime): Flow<AppStartCache>

    @Modifying
    @Query("DELETE FROM app_start_cache WHERE last_update < :timestamp")
    suspend fun deleteOlderThan(timestamp: OffsetDateTime): Int
}

@Repository
class AppStartCacheCountRepo(val client: DatabaseClient) {

    suspend fun count(monthStart: OffsetDateTime, weekStart: OffsetDateTime, dayStart: OffsetDateTime): CurrentAppStarts {
        val result = client.sql("""
                        SELECT
                            (SELECT COUNT(*) FROM app_start_cache WHERE last_update > :monthStart) as month,
                            (SELECT COUNT(*) FROM app_start_cache WHERE last_update > :weekStart) as week,
                            (SELECT COUNT(*) FROM app_start_cache WHERE last_update > :dayStart) as day
                        FROM app_start_cache
                    """.trimIndent())
                .bind("monthStart", monthStart)
                .bind("weekStart", weekStart)
                .bind("dayStart", dayStart)
                .fetch()
                .awaitFirst()

        return CurrentAppStarts(result["month"] as Long, result["week"] as Long, result["day"] as Long)
    }
}