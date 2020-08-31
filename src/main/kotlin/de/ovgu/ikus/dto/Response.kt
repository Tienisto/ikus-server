package de.ovgu.ikus.dto

import de.ovgu.ikus.model.LogType
import java.time.LocalDate
import java.time.OffsetDateTime

data class MeDto(val name: String, val admin: Boolean)
data class DashboardDto(val logs: List<LogDto>)

data class UserDto(val id: Int, val name: String)

data class ChannelDto(val id: Int, val name: LocalizedString)
data class AggregatedChannelDto(val post: List<ChannelDto>, val event: List<ChannelDto>)

data class PostDto(val id: Int, val channel: ChannelDto, val date: LocalDate, val title: LocalizedString, val content: LocalizedString)

data class LogDto(val user: UserDto?, val timestamp: OffsetDateTime, val type: LogType, val info: String)