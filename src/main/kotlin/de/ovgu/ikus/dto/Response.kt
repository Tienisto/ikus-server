package de.ovgu.ikus.dto

import java.time.LocalDate

data class MeDto(val name: String, val admin: Boolean)

data class UserDto(val id: Int, val name: String)

data class ChannelDto(val id: Int, val name: LocalizedString)

data class PostDto(val id: Int, val channel: ChannelDto, val date: LocalDate, val title: LocalizedString, val content: LocalizedString)