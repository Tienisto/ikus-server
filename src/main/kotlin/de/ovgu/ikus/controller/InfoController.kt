package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.DashboardDto
import de.ovgu.ikus.dto.LogDto
import de.ovgu.ikus.dto.MeDto
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.LogService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class InfoController (
        private val logService: LogService
) {

    @GetMapping("/me")
    fun me(authentication: Authentication): MeDto {
        val user = authentication.toUser()
        val admin = authentication.isAdmin()
        return MeDto(user.name, admin)
    }

    @GetMapping("/logs")
    suspend fun logs(@RequestParam limit: Int): List<LogDto> {
        return logService.findAll(limit)
    }

    @GetMapping("/dashboard")
    suspend fun dashboard(): DashboardDto {
        val logs = logService.findAll(10)
        return DashboardDto(logs)
    }
}