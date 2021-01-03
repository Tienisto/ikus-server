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
        ChannelType.LINK -> null
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
    return PostDto(id, channel, date.toString(), MultiLocaleString(en = title, de = titleDe), MultiLocaleString(en = content, de = contentDe), pinned, files)
}

fun PostFile.toDto(): PostFileDto {
    return PostFileDto(id, fileName)
}

fun Post.toLocalizedDto(locale: IkusLocale, channel: LocalizedChannelDto, files: List<PostFileDto>): LocalizedPostDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedPostDto(id, channel, date.toString(), title, Jsoup.parse(content).text().take(150) + "...", content, pinned, files)
        IkusLocale.DE -> LocalizedPostDto(id, channel, date.toString(), titleDe, Jsoup.parse(contentDe).text().take(150) + "...", contentDe, pinned, files)
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

fun MensaInfo.toLocalizedDto(locale: IkusLocale, menus: List<LocalizedMenuDto>): LocalizedMenuInfoDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedMenuInfoDto(name, openingHours, coords.toCoordsDto(), menus)
        IkusLocale.DE -> LocalizedMenuInfoDto(name, openingHoursDe, coords.toCoordsDto(), menus)
    }
}

fun Link.toDto(channel: ChannelDto): LinkDto {
    return LinkDto(id, channel, MultiLocaleString(en = url, de = urlDe), MultiLocaleString(en = info, de = infoDe))
}

fun Link.toLocalizedDto(locale: IkusLocale, channel: LocalizedChannelDto): LocalizedLinkDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedLinkDto(id, channel, url, info)
        IkusLocale.DE -> LocalizedLinkDto(id, channel, urlDe, infoDe)
    }
}

fun HandbookBookmark.toDto(): HandbookBookmarkDto {
    return HandbookBookmarkDto(page, name)
}

fun AudioFile.toDto(): AudioFileDto {
    val tempTextEn = text
    val tempTextDe = textDe
    val textMultiLocale = when {
        tempTextEn != null && tempTextDe != null -> MultiLocaleString(en = tempTextEn, de = tempTextDe)
        else -> null
    }
    return AudioFileDto(id, MultiLocaleString(en = name, de = nameDe), MultiLocaleString(en = file, de = fileDe), textMultiLocale)
}

fun AudioFile.toLocalizedDto(locale: IkusLocale): LocalizedAudioFileDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedAudioFileDto(id, name, file, text)
        IkusLocale.DE -> LocalizedAudioFileDto(id, nameDe, fileDe, textDe)
    }
}

fun Audio.toDto(files: List<AudioFileDto>): AudioDto {
    val tempImageEn = image
    val tempImageDe = imageDe
    val imageMultiLocale = when {
        tempImageEn != null && tempImageDe != null -> MultiLocaleString(en = tempImageEn, de = tempImageDe)
        else -> null
    }
    return AudioDto(id, MultiLocaleString(en = name, de = nameDe), imageMultiLocale, files)
}

fun Audio.toLocalizedDto(locale: IkusLocale, files: List<LocalizedAudioFileDto>): LocalizedAudioDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedAudioDto(id, name, image, files)
        IkusLocale.DE -> LocalizedAudioDto(id, nameDe, imageDe, files)
    }
}

fun Contact.toDto(): ContactDto {
    val tempOpeningHoursEn = openingHours
    val tempOpeningHoursDe = openingHoursDe
    val openingHoursMultiLocale = when {
        tempOpeningHoursEn != null && tempOpeningHoursDe != null -> MultiLocaleString(en = tempOpeningHoursEn, de = tempOpeningHoursDe)
        else -> null
    }
    return ContactDto(id, file, MultiLocaleString(en = name, de = nameDe), place, email, phoneNumber, openingHoursMultiLocale, links)
}

fun Contact.toLocalizedDto(locale: IkusLocale): LocalizedContactDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedContactDto(id, file, name, email, phoneNumber, place, openingHours, links)
        IkusLocale.DE -> LocalizedContactDto(id, file, nameDe, email, phoneNumber, place, openingHoursDe, links)
    }
}

fun Feature.toDto(post: PostDto?, link: LinkDto?): FeatureDto {
    val tempNameEn = name
    val tempNameDe = nameDe
    val nameMultiLocale = when {
        tempNameEn != null && tempNameDe != null -> MultiLocaleString(en = tempNameEn, de = tempNameDe)
        else -> null
    }
    return FeatureDto(id, favorite, nameMultiLocale, icon, nativeFeature, post, link)
}

fun Feature.toLocalizedDto(locale: IkusLocale, post: LocalizedPostDto?, link: LocalizedLinkDto?): LocalizedFeatureDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedFeatureDto(id, favorite, name, icon, nativeFeature, post, link)
        IkusLocale.DE -> LocalizedFeatureDto(id, favorite, nameDe, icon, nativeFeature, post, link)
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