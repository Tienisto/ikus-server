package de.ovgu.ikus.service

import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.AppStartCache
import de.ovgu.ikus.model.Platform
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.repository.AppStartCacheRepo
import de.ovgu.ikus.repository.AppStartRepo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class AnalyticsService (
        private val appStartCacheRepo: AppStartCacheRepo,
        private val appStartRepo: AppStartRepo
) {

    suspend fun handleAppStart(platform: Platform, deviceId: String) {
        val cached = appStartCacheRepo.findById(deviceId)
        if (cached == null) {
            // create new
            val newCached = AppStartCache(deviceId, platform, OffsetDateTime.now())
            newCached.setNewFlag()
            appStartCacheRepo.save(newCached)
        } else {
            // just update timestamp
            cached.lastUpdate = OffsetDateTime.now()
            appStartCacheRepo.save(cached)
        }
    }

    suspend fun findByType(type: StatsType): List<AppStart> {
        return appStartRepo.findByTypeOrderByDate(type).toList()
    }

    suspend fun countActiveUsersAfter(timestamp: OffsetDateTime): Int {
        return appStartCacheRepo.countByLastUpdateAfter(timestamp)
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