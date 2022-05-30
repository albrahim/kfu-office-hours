package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object Appointments : UUIDTable() {
    val doctor = reference("doctor", Users, ReferenceOption.CASCADE)
    val student = reference("student", Users, ReferenceOption.CASCADE)
    val startDateTime = varchar("startDateTime", 32)
    val endDateTime = varchar("endDateTime", 32)
}

class Appointment(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object: UUIDEntityClass<Appointment>(Appointments)
    var doctor by Appointments.doctor
    var student by Appointments.student
    var startDateTime by Appointments.startDateTime
    var endDateTime by Appointments.endDateTime

    val data get() = AppointmentData(
        id = id.toString(),
        doctor = doctor.value,
        student = student.value,
        startDateTime = startDateTime.toLocalDateTime(),
        endDateTime = endDateTime.toLocalDateTime()
    )
}

@Serializable
data class AppointmentData(
    val id: String,
    val doctor: Int,
    val student: Int,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
)

@Serializable
data class CreateAppointmentRequest(
    val doctor: Int,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
)