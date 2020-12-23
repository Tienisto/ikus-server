package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Podcast(@Id var id: Int = 0,
                   var name: String = "",
                   var image: String? = null,
                   var position: Int = 0)