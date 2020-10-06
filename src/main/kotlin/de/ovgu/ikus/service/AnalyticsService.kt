package de.ovgu.ikus.service

import de.ovgu.ikus.dto.CurrentAppStarts
import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.AppStartCache
import de.ovgu.ikus.model.Platform
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.repository.AppStartCacheCountRepo
import de.ovgu.ikus.repository.AppStartCacheRepo
import de.ovgu.ikus.repository.AppStartRepo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime

@Service
class AnalyticsService (
        private val appStartCacheCountRepo: AppStartCacheCountRepo,
        private val appStartCacheRepo: AppStartCacheRepo,
        private val appStartRepo: AppStartRepo
) {

    private val MIN_INTERVAL = Duration.ofHours(1)

    suspend fun handleAppStart(platform: Platform, deviceId: String) {
        val cached = appStartCacheRepo.findById(deviceId)
        if (cached == null) {
            // create new
            val newUser = AppStartCache(deviceId, platform, OffsetDateTime.now())
            newUser.setNewFlag()
            appStartCacheRepo.save(newUser)
        } else {

            val now = OffsetDateTime.now()

            if (Duration.between(cached.lastUpdate, now) < MIN_INTERVAL)
                return // ignore app start if it was too recently

            // just update timestamp
            cached.lastUpdate = now
            appStartCacheRepo.save(cached)
        }
    }

    suspend fun findByType(type: StatsType): List<AppStart> {
        return appStartRepo
                .findFirst90ByTypeOrderByDateDesc(type)
                .toList()
                .sortedBy { stats -> stats.date }
    }

    suspend fun findAppStartCacheAfter(timestamp: OffsetDateTime): List<AppStartCache> {
        return appStartCacheRepo.findByLastUpdateAfter(timestamp).toList()
    }

    suspend fun countAppStartCache(monthStart: OffsetDateTime, weekStart: OffsetDateTime, dayStart: OffsetDateTime): CurrentAppStarts {
        return appStartCacheCountRepo.count(monthStart, weekStart, dayStart)
    }

    suspend fun deleteAppStartCacheOlderThan(timestamp: OffsetDateTime) {
        appStartCacheRepo.deleteOlderThan(timestamp)
    }

    suspend fun saveAppStart(appStart: AppStart) {
        appStartRepo.save(appStart)
    }

    // dummy only
    suspend fun saveAllAppStartCaches(starts: List<AppStartCache>) {
        appStartCacheRepo.saveAll(starts).collect()
    }

    // dummy only
    suspend fun saveAllAppStarts(starts: List<AppStart>) {
        appStartRepo.saveAll(starts).collect()
    }

    suspend fun deleteAll() {
        appStartRepo.deleteAll()
        appStartCacheRepo.deleteAll()
    }
}