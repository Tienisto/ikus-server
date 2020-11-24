package de.ovgu.ikus.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

enum class NativeFeature (val favorite: Boolean) {
    MAP(true),
    MY_EVENTS(false),
    MENSA(true),
    LINKS(true),
    HANDBOOK(false),
    PODCASTS(false),
    FAQ(false),
    CONTACTS(false),
    EMAILS(false),
    GRADES(false),
    TIMETABLE(false)
}

@Table
data class Feature(@Id var id: Int = 0,
                   var position: Int = 0,
                   var favorite: Boolean = false,
                   var name: String? = null,
                   var nameDe: String? = null,
                   var icon: String? = null,
                   var nativeFeature: NativeFeature? = null,
                   var postId: Int? = null,
                   var linkId: Int? = null)