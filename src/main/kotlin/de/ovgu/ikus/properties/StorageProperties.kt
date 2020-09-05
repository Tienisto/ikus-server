package de.ovgu.ikus.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "storage")
class StorageProperties {
    var path: String = ""
    set(value) {
        val onlyForward = value.replace("\\", "/")
        val noLastSlash = when {
            onlyForward.endsWith("/") -> onlyForward.substring(0, onlyForward.length - 1)
            else -> onlyForward
        }
        field = noLastSlash
    }
}