package de.ovgu.ikus.model

import java.time.LocalDate

enum class MensaLocation {
    UNI_CAMPUS_DOWN,
    UNI_CAMPUS_UP,
    ZSCHOKKE,
    HERRENKRUG
}

data class Menu(val location: MensaLocation,
                val date: LocalDate,
                val food: List<Food>)