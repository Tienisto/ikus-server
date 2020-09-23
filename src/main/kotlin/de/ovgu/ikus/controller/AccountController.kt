package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.MeDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.HashService
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/account")
class AccountController (
        private val logService: LogService,
        private val hashService: HashService,
        private val userService: UserService
) {

    @GetMapping
    fun me(authentication: Authentication): MeDto {
        val user = authentication.toUser()
        val admin = authentication.isAdmin()
        return MeDto(user.name, admin)
    }

    @PostMapping("/password")
    suspend fun changePassword(authentication: Authentication, @RequestBody request: Request.UpdateMyPassword) {
        val user = authentication.toUser()

        if (!hashService.check(request.oldPassword, user.password))
            throw ErrorCode(403, "Wrong Password")

        userService.updatePassword(user, request.newPassword)
        logService.log(LogType.UPDATE_USER_PASSWORD, authentication.toUser(), user.name)
    }
}