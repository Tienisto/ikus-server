package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.dto.UserDto
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import de.ovgu.ikus.service.LogService
import de.ovgu.ikus.service.UserService
import de.ovgu.ikus.utils.toDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController (
        private val logService: LogService,
        private val userService: UserService
) {

    @GetMapping
    suspend fun getAllUsers(authentication: Authentication): List<UserDto> {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        return userService.findAllOrdered().map { user -> user.toDto() }
    }

    @PostMapping
    suspend fun addUser(authentication: Authentication, @RequestBody request: Request.AddUser) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        userService.addUser(request.name, request.password)
        logService.log(LogType.CREATE_USER, authentication.toUser(), request.name)
    }

    @PutMapping("/name")
    suspend fun updateName(authentication: Authentication, @RequestBody request: Request.UpdateName) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        val user = userService.findById(request.id) ?: throw ErrorCode(404, "user not found")
        val oldName = user.name
        userService.updateName(user, request.name)
        logService.log(LogType.UPDATE_USER_NAME, authentication.toUser(), oldName + " -> " + request.name)
    }

    @PutMapping("/password")
    suspend fun updatePassword(authentication: Authentication, @RequestBody request: Request.UpdatePassword) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        val user = userService.findById(request.id) ?: throw ErrorCode(404, "user not found")
        userService.updatePassword(user, request.password)
        logService.log(LogType.UPDATE_USER_PASSWORD, authentication.toUser(), user.name)
    }

    @DeleteMapping
    suspend fun deleteUser(authentication: Authentication, @RequestBody request: Request.Id) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        val user = userService.findById(request.id) ?: throw ErrorCode(404, "user not found")
        userService.delete(user)
        logService.log(LogType.DELETE_USER, authentication.toUser(), user.name)
    }
}