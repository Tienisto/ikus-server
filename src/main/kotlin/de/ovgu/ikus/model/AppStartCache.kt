package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

enum class Platform {
    ANDROID, IOS
}

@Table
data class AppStartCache(@Id var deviceId: String = "",
                         var platform: Platform = Platform.ANDROID,
                         var lastUpdate: OffsetDateTime = OffsetDateTime.MIN) : Persistable<String> {

    @Transient
    private var isActuallyNew = false
    fun setNewFlag() {
        isActuallyNew = true
    }

    override fun getId() = deviceId
    override fun isNew() = isActuallyNew
}