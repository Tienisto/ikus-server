package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "admin")
class AdminProperties {
    val name: String = "admin" // hardcoded admin user name
    var password: String = ""
}