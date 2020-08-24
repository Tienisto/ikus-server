package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.geo.Point
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table
data class Event(@Id var id: Int = 0,
                 var channelId: Int = 0,
                 var place: String? = null,
                 var coords: Point? = null,
                 var startTime: OffsetDateTime = OffsetDateTime.now(),
                 var endTime: OffsetDateTime? = null,
                 var name: String = "",
                 var nameDe: String = "",
                 var info: String? = null,
                 var infoDe: String? = null)