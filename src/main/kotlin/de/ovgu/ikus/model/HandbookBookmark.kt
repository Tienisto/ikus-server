package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class IkusLocale {
    EN, DE
}

@Table
data class HandbookBookmark(@Id var id: Int = 0,
                            var locale: IkusLocale = IkusLocale.EN,
                            var page: Int = 0,
                            var name: String = "")