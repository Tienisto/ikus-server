package de.ovgu.ikus.controller

import de.ovgu.ikus.security.isAdmin
import de.ovgu.ikus.security.toUser
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/channels")
class ChannelController {

    @GetMapping
    fun test(authentication: Authentication): String {
        val user = authentication.toUser()
        val admin = authentication.isAdmin()

        println(authentication.principal)
        println("$user - $admin")
        return "Hello"
    }
}