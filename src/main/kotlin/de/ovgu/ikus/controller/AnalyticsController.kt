package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.AppStartDto
import de.ovgu.ikus.dto.CurrentAppStarts
import de.ovgu.ikus.model.StatsType
import de.ovgu.ikus.service.AnalyticsService
import de.ovgu.ikus.utils.toDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController (
        private val analyticsService: AnalyticsService
) {

    @GetMapping("/app-starts")
    suspend fun getAppStarts(@RequestParam type: StatsType): List<AppStartDto> {
        return analyticsService.findByType(type).map { start -> start.toDto() }
    }

    @GetMapping("/app-starts/curr")
    suspend fun getCurrAppStarts(): CurrentAppStarts {
        val today = LocalDate.now(ZoneOffset.UTC)
        val monthStartTime = today
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime()
        val weekStartTime = today
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime()
        val dayStartTime = today
                .atStartOfDay(ZoneOffset.UTC)
                .toOffsetDateTime()

        val countMonthly = analyticsService.countActiveUsersAfter(monthStartTime)
        val countWeekly = analyticsService.countActiveUsersAfter(weekStartTime)
        val countDaily = analyticsService.countActiveUsersAfter(dayStartTime)
        return CurrentAppStarts(countMonthly, countWeekly, countDaily)
    }
}