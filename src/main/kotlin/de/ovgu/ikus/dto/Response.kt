package de.ovgu.ikus.dto

import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.JwtStatus
import java.time.OffsetDateTime

data class VersionDto(val version: String)
data class EnvDto(val key: String, val description: String, val default: String, val value: String)
data class StatusDto(val version: String, val date: String, val runTime: Long, val database: Boolean, val storageRead: Boolean, val storageWrite: Boolean, val adminPassword: Boolean, val jwtWebsite: JwtStatus, val jwtApp: JwtStatus, val env: List<EnvDto>?)

data class MeDto(val name: String, val admin: Boolean)
data class DashboardDto(val logs: List<LogDto>, val posts: List<PostDto>)

data class UserDto(val id: Int, val name: String)

data class ChannelDto(val id: Int, val name: LocalizedString)
data class AllChannelDto(val post: List<ChannelDto>, val event: List<ChannelDto>)

data class PostFileDto(val id: Int, val fileName: String)
data class PostDto(val id: Int, val channel: ChannelDto, val date: String, val title: LocalizedString, val content: LocalizedString, val files: List<PostFileDto>)
data class PostGroupDto(val channel: ChannelDto, val posts: List<PostDto>)

data class LogDto(val user: UserDto?, val timestamp: OffsetDateTime, val type: LogType, val info: String)

// public routes
data class LocalizedChannelDto(val id: Int, val name: String)
data class LocalizedPostDto(val id: Int, val channel: LocalizedChannelDto, val date: String, val title: String, val preview: String, val content: String, val files: List<PostFileDto>)
data class PublicPostDto(val channels: List<LocalizedChannelDto>, val posts: List<LocalizedPostDto>)
data class PublicFAQDto(val channel: LocalizedChannelDto, val posts: List<LocalizedPostDto>) // as array