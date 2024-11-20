package de.ovgu.ikus.model

import org.springframework.data.geo.Point

enum class Mensa {
    UNI_CAMPUS_DOWN,
    UNI_CAMPUS_UP,
    ZSCHOKKE,
    HERRENKRUG,
    PIER_16,
}

data class MensaInfo(val name: Mensa,
                     val openingHours: String,
                     val openingHoursDe: String,
                     val coords: Point,
                     val menus: List<Menu>)