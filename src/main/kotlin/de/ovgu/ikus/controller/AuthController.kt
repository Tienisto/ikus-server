package de.ovgu.ikus.controller

import de.ovgu.ikus.security.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController (
        private val authService: AuthService
) {

    @PostMapping("/login")
    suspend fun login() {
        authService.login("hi", "123")
    }
}