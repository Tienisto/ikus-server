package de.ovgu.ikus.model

enum class MensaLocation {
    UNI_CAMPUS_DOWN,
    UNI_CAMPUS_UP,
    ZSCHOKKE,
    HERRENKRUG
}

data class MensaInfo(val location: MensaLocation,
                     val menus: List<Menu>)