package com.example.office_hours_client.models

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.io.Serializable as SerializableLegacy

@Serializable
data class SlotData(
    val id: String,
    val startNano: Int,
    val endNano: Int,
    val dayOfWeek: DayOfWeek,
)

@Serializable
data class CreateSlotRequest(
    val startNano: Int,
    val endNano: Int,
    val dayOfWeek: DayOfWeek,
)

@Serializable
data class CreateSlotConflictResponse(
    val fail: String = "Conflicting slot",
    val conflictingSlot: String
)

@Serializable
data class ConcreteSlotData(
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
)