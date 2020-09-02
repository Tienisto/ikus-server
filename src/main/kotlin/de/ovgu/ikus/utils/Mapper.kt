package de.ovgu.ikus.utils

import de.ovgu.ikus.dto.*
import de.ovgu.ikus.model.*

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

fun Post.toDto(channel: ChannelDto): PostDto {
    return PostDto(id, channel, date, LocalizedString(en = title, de = titleDe), LocalizedString(en = content, de = contentDe))
}