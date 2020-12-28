package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class PodcastFile(@Id var id: Int = 0,
                       var podcastId: Int = 0,
                       var name: String = "",
                       var nameDe: String = "",
                       var file: String = "",
                       var fileDe: String = "",
                       var text: String? = null,
                       var textDe: String? = null,
                       var position: Int = 0)