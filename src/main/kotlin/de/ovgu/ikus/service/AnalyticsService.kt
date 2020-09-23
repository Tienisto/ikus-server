package de.ovgu.ikus.service

import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.AppStartCache
import de.ovgu.ikus.model.Platform
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.repository.AppStartCacheRepo
import de.ovgu.ikus.repository.AppStartRepo
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class AnalyticsService (
        private val appStartCacheRepo: AppStartCacheRepo,
        private val appStartRepo: AppStartRepo
) {

    private val logger = LoggerFactory.getLogger(AnalyticsService::class.java)

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

    // sec - min - hour - dayOfMonth - month - dayOfWeek
    // every 00:15 each first day of month
    @Scheduled(cron = "0 15 0 1 * *")
    fun tickMonthly() {
        countMonthly().subscribeOn(Schedulers.parallel()).subscribe()
    }

    // every 00:30 each monday
    @Scheduled(cron = "0 30 0 * * MON")
    fun tickWeekly() {
        countWeekly().subscribeOn(Schedulers.parallel()).subscribe()
    }

    private fun countMonthly(): Mono<Any> {
        return mono {
            logger.info("Start counting monthly users.")

            val appStarts = appStartCacheRepo.findByLastUpdateAfter(OffsetDateTime.now().minusMonths(1)).toList()

            val android = appStarts.count { start -> start.platform == Platform.ANDROID }
            val ios = appStarts.size - android

            val stats = AppStart(
                    type = StatsType.MONTHLY,
                    date = LocalDate.now(),
                    android = android,
                    ios = ios
            )

            appStartRepo.save(stats)
            val deleteCount = appStartCacheRepo.deleteOlderThan(OffsetDateTime.now().minusDays(60))

            logger.info("Result: $stats")
            logger.info("Deleted inactive users: $deleteCount")
        }
    }

    private fun countWeekly(): Mono<Any> {
        return mono {
            logger.info("Start counting weekly users.")

            val appStarts = appStartCacheRepo.findByLastUpdateAfter(OffsetDateTime.now().minusDays(7)).toList()

            val android = appStarts.count { start -> start.platform == Platform.ANDROID }
            val ios = appStarts.size - android

            val stats = AppStart(
                    type = StatsType.WEEKLY,
                    date = LocalDate.now(),
                    android = android,
                    ios = ios
            )

            appStartRepo.save(stats)
            logger.info("Result: $stats")
        }
    }
}