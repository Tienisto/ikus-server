package de.ovgu.ikus.utils

import java.net.URLConnection

fun getMime(path: String): String {
    return URLConnection.guessContentTypeFromName(path) ?: "application/octet-stream"
}