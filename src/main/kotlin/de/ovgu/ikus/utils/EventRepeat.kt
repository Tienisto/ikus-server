package de.ovgu.ikus.utils

import de.ovgu.ikus.dto.Request
import java.time.*

fun repeatDates(
    originalStartTime: OffsetDateTime,
    originalEndTime: OffsetDateTime?,
    interval: Request.EventInterval,
    endDate: LocalDate,
): List<Pair<OffsetDateTime, OffsetDateTime?>> {
    // We want end date to be inclusive
    val actualEndDate = endDate.plusDays(1)
    val newTimes = mutableListOf<Pair<OffsetDateTime, OffsetDateTime?>>()

    when (interval) {
        Request.EventInterval.WEEKLY, Request.EventInterval.BIWEEKLY -> {
            val weeksToAdd = when (interval) {
                Request.EventInterval.WEEKLY -> 1L
                Request.EventInterval.BIWEEKLY -> 2L
                else -> 1L
            }

            var i = 1L
            while (true) {
                val newStartTime = originalStartTime.plusWeeks(weeksToAdd * i)
                if (newStartTime.toLocalDate().isAfter(actualEndDate)) {
                    break
                }
                val newEndTime = originalEndTime?.plusWeeks(weeksToAdd * i)

                newTimes.add(Pair(newStartTime, newEndTime))

                i++
            }
        }

        Request.EventInterval.MONTHLY -> {
            var i = 1L
            while (true) {
                val newStartTime = originalStartTime.plusMonths(i)
                if (newStartTime.toLocalDate().isAfter(actualEndDate)) {
                    break
                }
                val newEndTime = originalEndTime?.plusMonths(i)

                newTimes.add(Pair(newStartTime, newEndTime))

                i++
            }
        }

        Request.EventInterval.MONTHLY_WEEKDAY -> {
            val originalDayOfWeek = originalStartTime.dayOfWeek
            val originalYearMonth = YearMonth.from(originalStartTime)
            val originalLocalDate = originalStartTime.toLocalDate()
            val originalDuration = originalEndTime?.let { Duration.between(originalStartTime, it) }

            val weekPosition = run {
                // Gather all occurrences of the same weekday in the original month
                val occurrencesInMonth = getOccurrencesInMonth(
                    date = originalYearMonth, dayOfWeek = originalDayOfWeek
                )

                val occurrenceIndex = occurrencesInMonth.indexOf(originalLocalDate)

                WeekPosition(
                    isLast = occurrenceIndex == occurrencesInMonth.size - 1,
                    position = occurrenceIndex,
                )
            }

            var i = 1L
            while (true) {
                val newMonth = originalYearMonth.plusMonths(i)
                val newDate = run {
                    val occurrencesInMonth = getOccurrencesInMonth(
                        date = newMonth, dayOfWeek = originalDayOfWeek
                    )

                    when {
                        weekPosition.isLast -> occurrencesInMonth.last()
                        weekPosition.position < occurrencesInMonth.size -> occurrencesInMonth[weekPosition.position]
                        else -> null
                    }
                }

                if (newDate == null || newDate.isAfter(actualEndDate)) {
                    break
                }

                val time = originalStartTime.toLocalTime()
                val zone = originalStartTime.offset

                val newStartTime = OffsetDateTime.of(newDate, time, zone)

                // Compute the new end time based on the original duration
                val newEndTime = originalDuration?.let { newStartTime.plus(it) }

                newTimes.add(Pair(newStartTime, newEndTime))

                i++
            }
        }
    }

    return newTimes
}

class WeekPosition(
    val isLast: Boolean,
    val position: Int,
)

fun getOccurrencesInMonth(
    date: YearMonth,
    dayOfWeek: DayOfWeek,
): List<LocalDate> {
    val temp = mutableListOf<LocalDate>()
    val lengthOfMonth = date.month.length(date.isLeapYear)
    for (day in 1..lengthOfMonth) {
        val currentDate = LocalDate.of(date.year, date.month, day)
        if (currentDate.dayOfWeek == dayOfWeek) {
            temp.add(currentDate)
        }
    }

    return temp
}
