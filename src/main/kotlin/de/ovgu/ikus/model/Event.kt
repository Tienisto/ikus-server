package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

enum class RegistrationField {
    FIRST_NAME, LAST_NAME, EMAIL, ADDRESS, COUNTRY
}

data class RegistrationData(
    val token: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val address: String?,
    val country: String?
)

@Table
data class Event(
    @Id var id: Int = 0,
    var channelId: Int = 0,
    var place: String? = null,
    var coords: Point? = null,
    var startTime: OffsetDateTime = OffsetDateTime.now(),
    var endTime: OffsetDateTime? = null,
    var name: String = "",
    var nameDe: String = "",
    var info: String? = null,
    var infoDe: String? = null,
    var registrationFields: List<RegistrationField> = emptyList(),
    var registrationSlots: Int = 0,
    var registrationSlotsWaiting: Int = 0,
    var registrationOpen: Boolean = false,
    var registrations: List<String> = emptyList() // encoded as RegistrationData
)