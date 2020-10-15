package de.ovgu.ikus.repository

import de.ovgu.ikus.dto.CurrentAppStarts
import de.ovgu.ikus.model.AppStartCache
import kotlinx.coroutines.flow.collect
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class AppStartCacheRepo(
        private val client: DatabaseClient,
        private val crudRepo: AppStartCacheCrudRepo
) {

    suspend fun findAfter(timestamp: OffsetDateTime): List<AppStartCache> {
        return crudRepo.findByLastUpdateAfter(timestamp)
    }

    // TODO: use database client when fixed
    suspend fun count(monthStart: OffsetDateTime, weekStart: OffsetDateTime, dayStart: OffsetDateTime): CurrentAppStarts {
        return CurrentAppStarts(
                month = crudRepo.countByLastUpdateAfter(monthStart).toLong(),
                week = crudRepo.countByLastUpdateAfter(weekStart).toLong(),
                day = crudRepo.countByLastUpdateAfter(dayStart).toLong()
        )
        /*
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
         */
    }

    suspend fun save(appStart: AppStartCache) {
        client.sql("""
                    INSERT INTO app_start_cache (device_id, platform, last_update) 
                    VALUES (:deviceId, :platform, :lastUpdate)
                    ON CONFLICT (device_id) DO UPDATE 
                    SET platform = :platform, 
                        last_update = :lastUpdate
                """.trimIndent())
                .bind("deviceId", appStart.deviceId)
                .bind("platform", appStart.platform.toString()) // TODO: remove toString
                .bind("lastUpdate", appStart.lastUpdate)
                .fetch()
                .awaitRowsUpdated()
    }

    suspend fun deleteOlderThan(timestamp: OffsetDateTime): Int {
        return client.sql("DELETE FROM app_start_cache WHERE last_update < :timestamp")
                .bind("timestamp", timestamp)
                .fetch()
                .awaitRowsUpdated()
    }

    // dummy only
    suspend fun saveAll(starts: List<AppStartCache>) {
        crudRepo.saveAll(starts).collect()
    }

    // dummy only
    suspend fun deleteAll() {
        client.sql("DELETE FROM app_start_cache").await()
    }
}

interface AppStartCacheCrudRepo : CoroutineCrudRepository<AppStartCache, String> {

    suspend fun findByLastUpdateAfter(timestamp: OffsetDateTime): List<AppStartCache>
    suspend fun countByLastUpdateAfter(timestamp: OffsetDateTime): Int
}