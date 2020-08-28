package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.dto.UserDto
import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController (
        private val userService: UserService
) {

    @GetMapping
    suspend fun getAllUsers(authentication: Authentication): List<UserDto> {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        return userService.findAll().map { user -> UserDto(user.id, user.name) }
    }

    @PostMapping
    suspend fun addUser(authentication: Authentication, @RequestBody request: Request.AddUser) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        userService.addUser(request.name, request.password)
    }

    @PutMapping
    suspend fun updateUser(authentication: Authentication, @RequestBody request: Request.UpdateUser) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        userService.updateUser(request.id, request.name, request.password)
    }

    @DeleteMapping
    suspend fun deleteUser(authentication: Authentication, @RequestBody request: Request.Id) {
        if (!authentication.isAdmin())
            throw ErrorCode(403, "Admin only")

        userService.deleteById(request.id)
    }
}