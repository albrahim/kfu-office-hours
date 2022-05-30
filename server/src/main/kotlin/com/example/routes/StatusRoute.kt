package com.example.routes

import com.example.extensions.clock
import com.example.extensions.local
import com.example.extensions.zone
import com.example.models.StatusResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statusRoute() {
    get {
        call.respond(StatusResponse(zone = zone, now = clock.now().local))
    }
}