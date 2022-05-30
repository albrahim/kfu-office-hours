package com.example.office_hours_client.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

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