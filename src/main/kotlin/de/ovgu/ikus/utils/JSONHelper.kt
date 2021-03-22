package de.ovgu.ikus.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())

    // keep offset data
    configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
}

/*
 * Extension function to convert anything to json
 * usage: val s = a.toJSON(), where a is any object
 */
fun Any.toJSON(): String {
    return mapper.writeValueAsString(this)
}

/*
 * Extension function to parse json
 * usage: val obj = s.parseJSON(), where s is a string
 */
inline fun <reified T> String.parseJSON(): T {
    return mapper.readValue(this)
}