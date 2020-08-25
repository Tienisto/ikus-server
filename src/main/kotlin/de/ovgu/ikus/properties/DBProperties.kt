package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "db")
class DBProperties {
    var url: String = ""
    var user: String = ""
    var password: String = ""
}