package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.Feedback
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.security.AuthService
import kotlinx.coroutines.delay
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController (
        private val authService: AuthService
) {

    @PostMapping("/login")
    suspend fun login(@RequestBody request: Request.Login): ResponseEntity<Feedback> {
        val token = authService.login(request.name, request.password)

        if (token == null) {
            delay(2000)
            throw ErrorCode(403, "Login failed")
        }

        return ResponseEntity.ok()
                .header("Set-Cookie", "jwt=$token")
                .body(Feedback())
    }
}