package de.ovgu.ikus.model

enum class MensaName {
    UNI_CAMPUS_DOWN,
    UNI_CAMPUS_UP,
    ZSCHOKKE,
    HERRENKRUG
}

data class MensaInfo(val name: MensaName,
                     val menus: List<Menu>)