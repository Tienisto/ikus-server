package de.ovgu.ikus.utils

fun String.trimOrNull(): String? {
    val trimmed = this.trim()
    return when {
        trimmed.isEmpty() -> null
        else -> trimmed
    }
}