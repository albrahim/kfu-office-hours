package com.example.extensions

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

const val millisecondsOnADay = 86400000
val dayRange = 0 until millisecondsOnADay

const val milliToNano = 1000000L
const val nanosecondsOnADay = millisecondsOnADay * milliToNano

val zone = TimeZone.currentSystemDefault()
val locale = Locale.getDefault()
val clock = Clock.System

val instantZero = Instant.fromEpochMilliseconds(0)
val datetimeZero = instantZero.toLocalDateTime(zone)

val Instant.local: LocalDateTime get() = this.toLocalDateTime(zone)
fun LocalDateTime.fromEpochMilliseconds(epochMilliseconds: Long) = Instant.fromEpochMilliseconds(epochMilliseconds).local
val LocalDateTime.instant get() = this.toInstant(zone)

val Number.localDateTime get() = Instant.fromEpochMilliseconds(this.toLong()).local