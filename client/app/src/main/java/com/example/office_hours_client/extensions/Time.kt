package com.example.office_hours_client.extensions

import kotlinx.datetime.LocalDate
import java.time.LocalTime

fun formatTimeRange(startTime: LocalTime, endTime: LocalTime, hour12: Boolean = true): String {
    fun ampm(time: LocalTime) = if (time.isBefore(LocalTime.NOON)) "AM" else "PM"

    val sh = if (hour12) startTime.hour % 12 else startTime.hour
    val eh = if (hour12) endTime.hour % 12 else endTime.hour

    val startHour = (if (sh == 0 && hour12) 12 else sh).toString().padStart(2, '0')
    val endHour = (if (eh == 0 && hour12) 12 else eh).toString().padStart(2, '0')
    val startMinute = startTime.minute.toString().padStart(2, '0')
    val endMinute = endTime.minute.toString().padStart(2, '0')

    val startTimeFormatted = "$startHour:$startMinute"
    val endTimeFormatted = "$endHour:$endMinute"

    if (!hour12) return "$startTimeFormatted - $endTimeFormatted"

    val startTimeAmpm = ampm(startTime)
    val endTimeAmpm = ampm(endTime)
    val optionalEndTimeAmpm = if (startTimeAmpm != endTimeAmpm) endTimeAmpm else ""

    val firstAmpm = if (optionalEndTimeAmpm.isNotBlank()) " $startTimeAmpm" else ""
    val lastAmpm = " $optionalEndTimeAmpm".ifBlank { " $startTimeAmpm" }


    return "$startTimeFormatted$firstAmpm - $endTimeFormatted$lastAmpm"
}

fun formatDate(date: LocalDate): String {
    return "${date.dayOfWeek}, ${date.month} ${date.dayOfMonth}"
}