package com.example.extensions

import com.example.models.ConcreteSlotData
import com.example.models.SlotData
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.*

fun concreteSlotsFromSlotData(slotData: Iterable<SlotData>) : List<ConcreteSlotData> {
    val now = clock.now().local
    val nowDate = now.date.toJavaLocalDate()
    val upcomingDates = (0..13L).map { nowDate.plusDays(it) }

    return slotData
        .asSequence()
        .map {
            Pair(it, upcomingDates)
        }
        .map { (slotData, upcomingDates) ->
            Pair(slotData, upcomingDates.filter { upcomingDate ->
                upcomingDate.dayOfWeek == slotData.dayOfWeek
            })
        }
        .map { (slotData, localDateList) ->
            localDateList.map { date ->
                val slotStartTime = LocalTime.ofNanoOfDay(slotData.startNano * milliToNano)
                val slotEndTime = LocalTime.ofNanoOfDay(slotData.endNano * milliToNano)

                val startDateTime = slotStartTime.atDate(date).toKotlinLocalDateTime()
                val endDateTime = slotEndTime.atDate(date).toKotlinLocalDateTime()
                ConcreteSlotData(startDateTime = startDateTime, endDateTime = endDateTime)
            }
        }
        .flatten()
        .filter { it.startDateTime > now }
        .sortedBy { it.startDateTime }
        .toList()
}

fun orderSlotData(list: List<SlotData>): List<SlotData> {
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val weekDays = (0 until 7L).associateBy { firstDay.plus(it) }
    return list
        .groupBy { it.dayOfWeek }
        .toList()
        .map {
            Pair(it.first, it.second.sortedBy { it.startNano })
        }.sortedBy { weekDays[it.first] }
        .map { it.second }
        .flatten()
}