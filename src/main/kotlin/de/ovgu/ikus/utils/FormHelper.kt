package de.ovgu.ikus.utils

/**
 * @return the trimmed string if not blank, null otherwise
 */
fun String.trimOrNull(): String? {
    val trimmed = this.trim()
    return when {
        trimmed.isEmpty() -> null
        else -> trimmed
    }
}