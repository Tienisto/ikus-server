package de.ovgu.ikus.utils

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import org.jsoup.Jsoup
import org.springframework.data.geo.Point

fun ChannelType.toPostType(): PostType? {
    return when (this) {
        ChannelType.NEWS -> PostType.NEWS
        ChannelType.FAQ -> PostType.FAQ
        ChannelType.CALENDAR -> null
    }
}

fun PostType.toChannelType(): ChannelType {
    return when (this) {
        PostType.NEWS -> ChannelType.NEWS
        PostType.FAQ -> ChannelType.FAQ
    }
}

// Model <-> Dto

fun User.toDto(): UserDto {
    return UserDto(id, name)
}

fun Log.toDto(user: UserDto?): LogDto {
    return LogDto(user, timestamp, type, info)
}

fun Channel.toDto(): ChannelDto {
    return ChannelDto(id, MultiLocaleString(en = name, de = nameDe))
}

fun Channel.toLocalizedDto(locale: IkusLocale): LocalizedChannelDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedChannelDto(id, name)
        IkusLocale.DE -> LocalizedChannelDto(id, nameDe)
    }
}

fun Post.toDto(channel: ChannelDto, files: List<PostFileDto>): PostDto {
    return PostDto(id, channel, date.toString(), MultiLocaleString(en = title, de = titleDe), MultiLocaleString(en = content, de = contentDe), files)
}

fun PostFile.toDto(): PostFileDto {
    return PostFileDto(id, fileName)
}

fun Post.toLocalizedDto(locale: IkusLocale, channel: LocalizedChannelDto, files: List<PostFileDto>): LocalizedPostDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedPostDto(id, channel, date.toString(), title, Jsoup.parse(content).text().take(300), content, files)
        IkusLocale.DE -> LocalizedPostDto(id, channel, date.toString(), titleDe, Jsoup.parse(contentDe).text().take(300), contentDe, files)
    }
}

fun Event.toDto(channel: ChannelDto): EventDto {
    val coordsDto = when (val temp = coords) {
        null -> null
        else -> CoordsDto(temp.x, temp.y)
    }
    val tempInfoEn = info
    val tempInfoDe = infoDe
    val infoMultiLocale = when {
        tempInfoEn != null && tempInfoDe != null -> MultiLocaleString(en = tempInfoEn, de = tempInfoDe)
        else -> null
    }
    return EventDto(id, channel, place, coordsDto, startTime.toString(), endTime?.toString(), MultiLocaleString(en = name, de = nameDe), infoMultiLocale)
}

fun Event.toLocalizedDto(locale: IkusLocale, channel: LocalizedChannelDto): LocalizedEventDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedEventDto(id, channel, name, info, startTime.toString(), endTime?.toString(), place, coords?.toCoordsDto())
        IkusLocale.DE -> LocalizedEventDto(id, channel, nameDe, infoDe, startTime.toString(), endTime?.toString(), place, coords?.toCoordsDto())
    }
}

// other

fun CoordsDto.toPoint(): Point {
    return Point(x, y)
}

fun Point.toCoordsDto(): CoordsDto {
    return CoordsDto(x, y)
}