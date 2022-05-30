package com.example.office_hours_client.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val zone: TimeZone,
    val now: LocalDateTime
)