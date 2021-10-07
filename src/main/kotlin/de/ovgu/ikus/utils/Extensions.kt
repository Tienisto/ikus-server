package de.ovgu.ikus.utils

import java.time.OffsetDateTime
import java.time.ZoneId

private val germanTimezone = ZoneId.of("Europe/Berlin")

fun OffsetDateTime.germany(): OffsetDateTime {
    return this.withOffsetSameInstant(germanTimezone.rules.getOffset(this.toInstant()))
}