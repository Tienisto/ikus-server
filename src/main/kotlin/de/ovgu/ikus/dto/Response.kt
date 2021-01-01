package de.ovgu.ikus.dto

import com.fasterxml.jackson.annotation.JsonInclude
import de.ovgu.ikus.model.FoodTag
import de.ovgu.ikus.model.LogType
import de.ovgu.ikus.model.Mensa
import de.ovgu.ikus.model.NativeFeature
import de.ovgu.ikus.security.JwtStatus
import java.time.OffsetDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Feedback(val message: String? = null)
data class MultiLocaleString(val en: String, val de: String)
data class Token(val token: String)

data class VersionDto(val version: String)
data class EnvDto(val key: String, val description: String, val default: String, val value: String)
data class StatusDto(val version: String, val date: String, val runTime: Long, val database: Boolean, val storageRead: Boolean, val storageWrite: Boolean, val adminPassword: Boolean, val jwtWebsite: JwtStatus, val jwtApp: JwtStatus, val env: List<EnvDto>?)
data class SysLogsDto(val logs: String)

data class MeDto(val name: String, val admin: Boolean)
data class DashboardDto(val logs: List<LogDto>, val posts: List<PostDto>, val events: List<EventDto>)
data class LogDto(val user: UserDto?, val timestamp: OffsetDateTime, val type: LogType, val info: String)
data class UserDto(val id: Int, val name: String)

// channel
data class ChannelDto(val id: Int, val name: MultiLocaleString)
data class AllChannelDto(val post: List<ChannelDto>, val event: List<ChannelDto>)

// post
data class PostFileDto(val id: Int, val fileName: String)
data class PostDto(val id: Int, val channel: ChannelDto, val date: String, val title: MultiLocaleString, val content: MultiLocaleString, val pinned: Boolean, val files: List<PostFileDto>)
data class NewsDto(val posts: List<PostDto>, val pinned: List<PostDto>)
data class PostGroupDto(val channel: ChannelDto, val posts: List<PostDto>)

// event
data class CoordsDto(val x: Double, val y: Double)
data class EventDto(val id: Int, val channel: ChannelDto, val place: String?, val coords: CoordsDto?, val startTime: String, val endTime: String?, val name: MultiLocaleString, val info: MultiLocaleString?)

// links
data class LinkDto(val id: Int, val channel: ChannelDto, val url: MultiLocaleString, val info: MultiLocaleString)
data class LinkGroupDto(val channel: ChannelDto, val links: List<LinkDto>)

// handbook
data class HandbookBookmarkDto(val page: Int, val name: String)

// audio
data class AudioFileDto(val id: Int, val name: MultiLocaleString, val file: MultiLocaleString, val text: MultiLocaleString?)
data class AudioDto(val id: Int, val name: MultiLocaleString, val image: MultiLocaleString?, val files: List<AudioFileDto>)

// contacts
data class ContactDto(val id: Int, val file: String?, val name: MultiLocaleString, val place: String?, val email: String?, val phoneNumber: String?, val openingHours: MultiLocaleString?, val links: List<String>)

// features
data class FeatureDto(val id: Int, val favorite: Boolean, val name: MultiLocaleString?, val icon: String?, val nativeFeature: NativeFeature?, val post: PostDto?, val link: LinkDto?)

// analytics
data class AppStartDto(val date: String, val android: Int, val ios: Int)
data class CurrentAppStarts(val month: Long, val week: Long, val day: Long)

// public routes
data class LocalizedChannelDto(val id: Int, val name: String)
data class LocalizedPostDto(val id: Int, val channel: LocalizedChannelDto, val date: String, val title: String, val preview: String, val content: String, val pinned: Boolean, val files: List<PostFileDto>)
data class LocalizedEventDto(val id: Int, val channel: LocalizedChannelDto, val name: String, val info: String?, val startTime: String, val endTime: String?, val place: String?, val coords: CoordsDto?)
data class LocalizedLinkDto(val id: Int, val channel: LocalizedChannelDto, val url: String, val info: String)
data class LocalizedContactDto(val id: Int, val file: String?, val name: String, val email: String?, val phoneNumber: String?, val place: String?, val openingHours: String?, val links: List<String>)
data class LocalizedFoodDto(val name: String, val price: Double?, val tags: List<FoodTag>)
data class LocalizedMenuDto(val date: String, val food: List<LocalizedFoodDto>)
data class LocalizedMenuInfoDto(val name: Mensa, val openingHours: String, val coords: CoordsDto, val menus: List<LocalizedMenuDto>) // as array
data class LocalizedFeatureDto(val id: Int, val favorite: Boolean, val name: String?, val icon: String?, val nativeFeature: NativeFeature?, val post: LocalizedPostDto?, val link: LocalizedLinkDto?)

data class PublicPostDto(val channels: List<LocalizedChannelDto>, val posts: List<LocalizedPostDto>)
data class PublicEventDto(val channels: List<LocalizedChannelDto>, val events: List<LocalizedEventDto>)
data class PublicLinkDto(val channel: LocalizedChannelDto, val links: List<LocalizedLinkDto>) // as array
data class PublicFAQDto(val channel: LocalizedChannelDto, val posts: List<LocalizedPostDto>) // as array
data class PublicConfigDto(val version: Int, val features: List<LocalizedFeatureDto>)