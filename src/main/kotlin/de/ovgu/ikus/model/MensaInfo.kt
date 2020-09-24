package de.ovgu.ikus.model

enum class Mensa {
    UNI_CAMPUS_DOWN,
    UNI_CAMPUS_UP,
    ZSCHOKKE,
    HERRENKRUG
}

data class MensaInfo(val name: Mensa,
                     val menus: List<Menu>)