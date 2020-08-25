package de.ovgu.ikus.properties

import org.springframework.context.annotation.Configuration

@Configuration
class JwtProperties {
    var timeout: Long = 0
}