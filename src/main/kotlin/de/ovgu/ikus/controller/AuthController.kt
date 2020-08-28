package de.ovgu.ikus.controller

import de.ovgu.ikus.dto.ErrorCode
import de.ovgu.ikus.dto.MeDto
import de.ovgu.ikus.dto.Request
import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.security.AuthService
import de.ovgu.ikus.security.JwtService
import kotlinx.coroutines.delay
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthController (
        private val propsAdmin: AdminProperties,
        private val authService: AuthService,
        private val jwtService: JwtService
) {

    @PostMapping("/login")
    suspend fun login(@RequestBody request: Request.Login): ResponseEntity<MeDto> {
        val user = authService.loginChallenge(request.name, request.password)

        if (user == null) {
            delay(2000)
            throw ErrorCode(403, "Login failed")
        }

        val token = jwtService.createToken(user)
        val admin = user.name == propsAdmin.name

        return ResponseEntity.ok()
                .header("Set-Cookie", "jwt=$token; Path=/")
                .body(MeDto(user.name, admin))
    }

    @PostMapping("/logout")
    suspend fun logout(): ResponseEntity<Any> {
        return ResponseEntity.ok()
                .header("Set-Cookie", "jwt=an invalid jwt; Path=/")
                .build()
    }
}