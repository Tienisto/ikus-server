package de.ovgu.ikus.utils

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*
import org.jsoup.Jsoup

fun ChannelType.toPostType(): PostType? {
    return when (this) {
        ChannelType.NEWS -> PostType.NEWS
        ChannelType.FAQ -> PostType.FAQ
        ChannelType.CALENDAR -> null
    }
}

fun User.toDto(): UserDto {
    return UserDto(id, name)
}

fun Log.toDto(user: UserDto?): LogDto {
    return LogDto(user, timestamp, type, info)
}

fun Channel.toDto(): ChannelDto {
    return ChannelDto(id, LocalizedString(en = name, de = nameDe))
}

fun Channel.toLocalizedDto(locale: IkusLocale): LocalizedChannelDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedChannelDto(id, name)
        IkusLocale.DE -> LocalizedChannelDto(id, nameDe)
    }
}

fun Post.toDto(channel: ChannelDto, files: List<PostFileDto>): PostDto {
    return PostDto(id!!, channel, date.toString(), LocalizedString(en = title, de = titleDe), LocalizedString(en = content, de = contentDe), files)
}

fun PostFile.toDto(): PostFileDto {
    return PostFileDto(id!!, fileName)
}

fun Post.toLocalizedDto(locale: IkusLocale, channel: LocalizedChannelDto, files: List<PostFileDto>): LocalizedPostDto {
    return when (locale) {
        IkusLocale.EN -> LocalizedPostDto(id!!, channel, date.toString(), title, Jsoup.parse(content).text().take(300), content, files)
        IkusLocale.DE -> LocalizedPostDto(id!!, channel, date.toString(), titleDe, Jsoup.parse(contentDe).text().take(300), contentDe, files)
    }
}