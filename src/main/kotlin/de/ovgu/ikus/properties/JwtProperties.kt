package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    var website: String = ""
    var app: String = ""
    var timeout: Long = 0
}