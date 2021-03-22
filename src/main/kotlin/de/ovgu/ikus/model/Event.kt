package de.ovgu.ikus.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

enum class RegistrationField {
    MATRICULATION_NUMBER, FIRST_NAME, LAST_NAME, EMAIL, ADDRESS, COUNTRY
}

data class RegistrationData(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mmXXX")
    val timestamp: OffsetDateTime,
    val token: String,
    val matriculationNumber: Int?,
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
    var registrationFields: List<String> = emptyList(),
    var registrationSlots: Int = 0,
    var registrationSlotsWaiting: Int = 0,
    var registrationOpen: Boolean = false,
    var registrations: List<String> = emptyList() // encoded as RegistrationData
)