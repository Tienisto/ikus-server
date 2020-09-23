package de.ovgu.ikus.dto

import de.ovgu.ikus.model.IkusLocale
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.security.JwtStatus
import java.time.OffsetDateTime

data class VersionDto(val version: String)
data class EnvDto(val key: String, val description: String, val default: String, val value: String)
data class StatusDto(val version: String, val date: String, val runTime: Long, val database: Boolean, val storageRead: Boolean, val storageWrite: Boolean, val adminPassword: Boolean, val jwtWebsite: JwtStatus, val jwtApp: JwtStatus, val env: List<EnvDto>?)

data class MeDto(val name: String, val admin: Boolean)
data class DashboardDto(val logs: List<LogDto>, val posts: List<PostDto>, val events: List<EventDto>)
data class LogDto(val user: UserDto?, val timestamp: OffsetDateTime, val type: LogType, val info: String)
data class UserDto(val id: Int, val name: String)

// channel
data class ChannelDto(val id: Int, val name: MultiLocaleString)
data class AllChannelDto(val post: List<ChannelDto>, val event: List<ChannelDto>)

// post
data class PostFileDto(val id: Int, val fileName: String)
data class PostDto(val id: Int, val channel: ChannelDto, val date: String, val title: MultiLocaleString, val content: MultiLocaleString, val files: List<PostFileDto>)
data class PostGroupDto(val channel: ChannelDto, val posts: List<PostDto>)

// event
data class CoordsDto(val x: Double, val y: Double)
data class EventDto(val id: Int, val channel: ChannelDto, val place: String?, val coords: CoordsDto?, val startTime: String, val endTime: String?, val name: MultiLocaleString, val info: MultiLocaleString?)

// links
data class LinkGroupDto(val id: Int, val name: MultiLocaleString)
data class LinkDto(val id: Int, val group: LinkGroupDto, val url: MultiLocaleString, val info: MultiLocaleString)
data class LinkGroupWithLinksDto(val group: LinkGroupDto, val links: List<LinkDto>)

// handbook
data class HandbookBookmarkDto(val page: Int, val name: String)
data class HandbookBookmarkGroupedDto(val locale: IkusLocale, val bookmarks: List<HandbookBookmarkDto>)

// contacts
data class ContactDto(val id: Int, val file: String?, val name: MultiLocaleString, val email: String?, val phoneNumber: String?, val place: String?, val openingHours: MultiLocaleString?)

// public routes
data class LocalizedChannelDto(val id: Int, val name: String)
data class LocalizedPostDto(val id: Int, val channel: LocalizedChannelDto, val date: String, val title: String, val preview: String, val content: String, val files: List<PostFileDto>)
data class LocalizedEventDto(val id: Int, val channel: LocalizedChannelDto, val name: String, val info: String?, val startTime: String, val endTime: String?, val place: String?, val coords: CoordsDto?)
data class LocalizedLinkGroupDto(val id: Int, val name: String)
data class LocalizedLinkDto(val id: Int, val group: LocalizedLinkGroupDto, val url: String, val info: String)
data class LocalizedContactDto(val id: Int, val file: String?, val name: String, val email: String?, val phoneNumber: String?, val place: String?, val openingHours: String?)

data class PublicPostDto(val channels: List<LocalizedChannelDto>, val posts: List<LocalizedPostDto>)
data class PublicEventDto(val channels: List<LocalizedChannelDto>, val events: List<LocalizedEventDto>)
data class PublicLinkDto(val group: LocalizedLinkGroupDto, val links: List<LocalizedLinkDto>) // as array
data class PublicFAQDto(val channel: LocalizedChannelDto, val posts: List<LocalizedPostDto>) // as array