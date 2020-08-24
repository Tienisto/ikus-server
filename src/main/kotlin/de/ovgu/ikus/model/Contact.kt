package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table
data class Contact(@Id var id: Int = 0,
                   var email: String? = null,
                   var phoneNumber: String? = null,
                   var place: String? = null,
                   var name: String = "",
                   var nameDe: String = "",
                   var openingHours: String? = null,
                   var openingHoursDe: String? = null)