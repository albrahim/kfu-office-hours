package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun Application.configureStatus() {
    install(StatusPages) {
        exception<kotlinx.serialization.SerializationException> { call, cause ->
            call.respondText(text = "400: Bad Request", status = HttpStatusCode.BadRequest)
        }
    }
}