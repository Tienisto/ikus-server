package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("ikus_user")
data class User(@Id var id: Int = 0,
                var name: String = "",
                var password: String = "")