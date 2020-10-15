package de.ovgu.ikus.service

import de.ovgu.ikus.dto.CurrentAppStarts
import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.AppStartCache
import de.ovgu.ikus.model.Platform
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.repository.AppStartCacheRepo
import de.ovgu.ikus.repository.AppStartRepo
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class AnalyticsService (
        private val appStartCacheRepo: AppStartCacheRepo,
        private val appStartRepo: AppStartRepo
) {

    suspend fun saveAppStartCache(platform: Platform, deviceId: String) {
        val appStart = AppStartCache(deviceId, platform, OffsetDateTime.now())
        appStartCacheRepo.save(appStart)
    }

    suspend fun findByType(type: StatsType): List<AppStart> {
        return appStartRepo
                .findFirst90ByTypeOrderByDateDesc(type)
                .sortedBy { stats -> stats.date }
    }

    suspend fun findAppStartCacheAfter(timestamp: OffsetDateTime): List<AppStartCache> {
        return appStartCacheRepo.findAfter(timestamp)
    }

    suspend fun countAppStartCache(monthStart: OffsetDateTime, weekStart: OffsetDateTime, dayStart: OffsetDateTime): CurrentAppStarts {
        return appStartCacheRepo.count(monthStart, weekStart, dayStart)
    }

    suspend fun deleteAppStartCacheOlderThan(timestamp: OffsetDateTime) {
        appStartCacheRepo.deleteOlderThan(timestamp)
    }

    suspend fun saveAppStart(appStart: AppStart) {
        appStartRepo.save(appStart)
    }

    // dummy only
    suspend fun saveAllAppStartCaches(starts: List<AppStartCache>) {
        appStartCacheRepo.saveAll(starts)
    }

    // dummy only
    suspend fun saveAllAppStarts(starts: List<AppStart>) {
        appStartRepo.saveAll(starts).collect()
    }

    // dummy only
    suspend fun deleteAll() {
        appStartRepo.deleteAll()
        appStartCacheRepo.deleteAll()
    }
}