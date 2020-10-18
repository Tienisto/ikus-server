package de.ovgu.ikus.controller

import de.ovgu.ikus.properties.AdminProperties
import de.ovgu.ikus.security.JwtService
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI

@Controller
class VueController (
        private val propsAdmin: AdminProperties,
        private val jwtService: JwtService
) {

    @GetMapping("/" , "/privacy", "/status",
            "/users", "/statistics", "/activities", "sys-logs",
            "/dashboard", "/posts", "/calendar", "/channels", "/links", "/handbook", "/faq", "/contacts", "/features")
    @ResponseBody
    suspend fun routes(request: ServerHttpRequest, response: ServerHttpResponse): ClassPathResource? {

        val token = request.cookies["jwt"]?.firstOrNull()?.value
        val user = jwtService.getUser(token)
        val path = request.path.pathWithinApplication().value()

        when {
            user != null && path == "/" -> {
                // redirect to dashboard if already logged in
                response.statusCode = HttpStatus.TEMPORARY_REDIRECT
                response.headers.location = when (user.name) {
                    propsAdmin.name -> URI.create("/users")
                    else -> URI.create("/dashboard")
                }
                return null
            }

            user == null && path != "/" && path != "/privacy" && path != "/status" -> {
                // redirect to login if not logged in
                response.statusCode = HttpStatus.TEMPORARY_REDIRECT
                response.headers.location = URI.create("/")
                return null
            }
        }

        return ClassPathResource("static/index.html")
    }
}