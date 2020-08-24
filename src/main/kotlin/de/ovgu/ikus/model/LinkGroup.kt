package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class LinkGroup(@Id var id: Int = 0,
                     var name: String = "",
                     var nameDe: String = "")