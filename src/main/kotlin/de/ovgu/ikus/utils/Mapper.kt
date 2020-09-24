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

fun Food.toLocalizedDto(locale: IkusLocale): LocalizedFoodDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedFoodDto(name, price, tags)
        IkusLocale.DE -> LocalizedFoodDto(nameDe, price, tags)
    }
}

fun Menu.toLocalizedDto(food: List<LocalizedFoodDto>): LocalizedMenuDto {
    return LocalizedMenuDto(date.toString(), food)
}

fun LinkGroup.toDto(): LinkGroupDto {
    return LinkGroupDto(id, MultiLocaleString(en = name, de = nameDe))
}

fun LinkGroup.toLocalizedDto(locale: IkusLocale): LocalizedLinkGroupDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedLinkGroupDto(id, name)
        IkusLocale.DE -> LocalizedLinkGroupDto(id, nameDe)
    }
}

fun Link.toDto(group: LinkGroupDto): LinkDto {
    return LinkDto(id, group, MultiLocaleString(en = url, de = urlDe), MultiLocaleString(en = info, de = infoDe))
}

fun Link.toLocalizedDto(locale: IkusLocale, group: LocalizedLinkGroupDto): LocalizedLinkDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedLinkDto(id, group, url, info)
        IkusLocale.DE -> LocalizedLinkDto(id, group, urlDe, infoDe)
    }
}

fun HandbookBookmark.toDto(): HandbookBookmarkDto {
    return HandbookBookmarkDto(page, name)
}

fun Contact.toDto(): ContactDto {
    val tempOpeningHoursEn = openingHours
    val tempOpeningHoursDe = openingHoursDe
    val openingHoursMultiLocale = when {
        tempOpeningHoursEn != null && tempOpeningHoursDe != null -> MultiLocaleString(en = tempOpeningHoursEn, de = tempOpeningHoursDe)
        else -> null
    }
    return ContactDto(id, file, MultiLocaleString(en = name, de = nameDe), email, phoneNumber, place, openingHoursMultiLocale)
}

fun Contact.toLocalizedDto(locale: IkusLocale): LocalizedContactDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedContactDto(id, file, name, email, phoneNumber, place, openingHours)
        IkusLocale.DE -> LocalizedContactDto(id, file, nameDe, email, phoneNumber, place, openingHoursDe)
    }
}

fun AppStart.toDto(): AppStartDto {
    return AppStartDto(date.toString(), android, ios)
}

// other

fun CoordsDto.toPoint(): Point {
    return Point(x, y)
}

fun Point.toCoordsDto(): CoordsDto {
    return CoordsDto(x, y)
}