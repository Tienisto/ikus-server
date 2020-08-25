package de.ovgu.ikus.properties

import org.springframework.context.annotation.Configuration

@Configuration
class JwtProperties {
    var key: String = ""
    var timeout: Long = 0
}