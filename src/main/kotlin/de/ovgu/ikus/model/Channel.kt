package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class ChannelType {
    NEWS, EVENT, FAQ
}

@Table
data class Channel(@Id var id: Int = 0,
                   var type: ChannelType = ChannelType.NEWS,
                   var name: String = "",
                   var nameDe: String = "")