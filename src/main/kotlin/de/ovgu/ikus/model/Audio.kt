package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Audio(@Id var id: Int = 0,
                 var name: String = "",
                 var nameDe: String = "",
                 var image: String? = null,
                 var imageDe: String? = null,
                 var position: Int = 0)