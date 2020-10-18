package de.ovgu.ikus.scheduler

import de.ovgu.ikus.service.MensaService
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import javax.annotation.PostConstruct

@Component
class MensaScheduler (
        private val mensaService: MensaService
) {

    private val logger = LoggerFactory.getLogger(MensaScheduler::class.java)

    @PostConstruct
    fun init() {
        tick() // also trigger update upon server start
    }

    // sec - min - hour - dayOfMonth - month - dayOfWeek
    // every half hour
    @Scheduled(cron = "0 0,30 * * * *")
    fun tick() {
        mono {
            logger.debug("Updating mensa cache.")
            mensaService.updateMenu()
            logger.debug("Mensa cache updated.")
        }.subscribeOn(Schedulers.parallel()).subscribe()
    }
}