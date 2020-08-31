package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

enum class Platform {
    ANDROID, IOS
}

@Table
data class AppStartCache(@Id var deviceId: String = "",
                         var platform: Platform = Platform.ANDROID,
                         var lastLogin: OffsetDateTime = OffsetDateTime.MIN)