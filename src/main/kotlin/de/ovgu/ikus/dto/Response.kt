package de.ovgu.ikus.dto

import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.JwtStatus
import java.time.LocalDate
import java.time.OffsetDateTime

data class VersionDto(val version: String)
data class StatusDto(val version: String, val date: String, val runTime: Long, val database: Boolean, val storageRead: Boolean, val storageWrite: Boolean, val adminPassword: Boolean, val jwt: JwtStatus)

data class MeDto(val name: String, val admin: Boolean)
data class DashboardDto(val logs: List<LogDto>, val posts: List<PostDto>)

data class UserDto(val id: Int, val name: String)

data class ChannelDto(val id: Int, val name: LocalizedString)
data class AllChannelDto(val post: List<ChannelDto>, val event: List<ChannelDto>)

data class PostDto(val id: Int, val channel: ChannelDto, val date: String, val title: LocalizedString, val content: LocalizedString)

data class LogDto(val user: UserDto?, val timestamp: OffsetDateTime, val type: LogType, val info: String)

// public routes
data class LocalizedChannelDto(val id: Int, val name: String)
data class LocalizedPostDto(val id: Int, val channel: LocalizedChannelDto, val date: String, val title: String, val preview: String, val content: String)
data class PublicPostDto(val channels: List<LocalizedChannelDto>, val posts: List<LocalizedPostDto>)