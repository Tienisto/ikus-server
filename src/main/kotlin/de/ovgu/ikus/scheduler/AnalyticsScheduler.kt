package de.ovgu.ikus.scheduler

import de.ovgu.ikus.model.AppStart
import de.ovgu.ikus.model.Platform
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.repository.AppStartCacheRepo
import de.ovgu.ikus.repository.AppStartRepo
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.temporal.TemporalAmount

@Component
class AnalyticsScheduler (
        private val appStartCacheRepo: AppStartCacheRepo,
        private val appStartRepo: AppStartRepo
) {

    private val logger = LoggerFactory.getLogger(AnalyticsScheduler::class.java)

    // sec - min - hour - dayOfMonth - month - dayOfWeek
    // every 00:10 each day
    @Scheduled(cron = "0 10 0 * * *")
    fun tickDaily() {
        countGeneric(StatsType.DAILY, Period.ofDays(1)).subscribeOn(Schedulers.parallel()).subscribe()
    }

    // every 00:20 each monday
    @Scheduled(cron = "0 20 0 * * MON")
    fun tickWeekly() {
        countGeneric(StatsType.WEEKLY, Period.ofWeeks(1)).subscribeOn(Schedulers.parallel()).subscribe()
    }

    // every 00:30 each first day of month
    @Scheduled(cron = "0 30 0 1 * *")
    fun tickMonthly() {
        countGeneric(StatsType.MONTHLY, Period.ofMonths(1), true).subscribeOn(Schedulers.parallel()).subscribe()
    }

    private fun countGeneric(type: StatsType, time: TemporalAmount, clearUsers: Boolean = false): Mono<Any> {
        return mono {
            logger.info("Start counting.")

            val appStarts = appStartCacheRepo.findByLastUpdateAfter(OffsetDateTime.now().minus(time)).toList()

            val android = appStarts.count { start -> start.platform == Platform.ANDROID }
            val ios = appStarts.size - android

            val stats = AppStart(
                    type = type,
                    date = LocalDate.now(),
                    android = android,
                    ios = ios
            )

            appStartRepo.save(stats)
            logger.info(stats.toString())

            if (clearUsers) {
                val deleteCount = appStartCacheRepo.deleteOlderThan(OffsetDateTime.now().minusDays(60))
                logger.info("Deleted inactive users: $deleteCount")
            }
        }
    }
}