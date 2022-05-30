package com.example.models

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object Slots : UUIDTable() {
    val doctor = reference("doctor", Users, onDelete = ReferenceOption.CASCADE)
    val startNano = integer("startNano")
    val endNano = integer("endNano")
    val dayOfWeek = enumeration("dayOfWeek", DayOfWeek::class)
}

class Slot(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object: UUIDEntityClass<Slot>(Slots)
    var doctor by Slots.doctor
    var startNano by Slots.startNano
    var endNano by Slots.endNano
    var dayOfWeek by Slots.dayOfWeek

    val data get() = SlotData(id.toString(), startNano, endNano, dayOfWeek)
}

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