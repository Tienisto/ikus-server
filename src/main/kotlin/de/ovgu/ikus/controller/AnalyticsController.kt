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
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
        val now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC)
        val month = analyticsService.countActiveUsersAfter(now.minusMonths(1))
        val week = analyticsService.countActiveUsersAfter(now.minusWeeks(1))
        val day = analyticsService.countActiveUsersAfter(OffsetDateTime.of(now.year, now.monthValue, now.dayOfMonth, 0, 0, 0, 0, ZoneOffset.UTC))
        return CurrentAppStarts(month, week, day)
    }
}