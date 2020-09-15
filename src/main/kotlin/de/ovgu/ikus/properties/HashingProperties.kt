package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "hashing")
class HashingProperties {
    var bcryptStrength: Int = 0
}