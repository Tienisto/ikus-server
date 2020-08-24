package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class ChannelType {
    POST, EVENT, FAQ
}

@Table
data class Channel(@Id var id: Int = 0,
                   var type: ChannelType = ChannelType.POST,
                   var name: String = "",
                   var nameDe: String = "")