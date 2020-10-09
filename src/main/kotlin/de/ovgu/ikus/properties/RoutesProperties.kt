package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "routes")
class RoutesProperties {
    var dev: Boolean = false
}