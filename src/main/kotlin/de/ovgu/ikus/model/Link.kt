package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Link(@Id var id: Int = 0,
                var channelId: Int = 0,
                var position: Int = 0,
                var url: String = "",
                var urlDe: String = "",
                var info: String = "",
                var infoDe: String = "")