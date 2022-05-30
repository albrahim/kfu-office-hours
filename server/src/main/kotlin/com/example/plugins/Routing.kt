package com.example.plugins

import com.example.extensions.zone
import com.example.models.StatusResponse
import com.example.routes.appointmentRoutes
import com.example.routes.slotRoutes
import com.example.routes.statusRoute
import com.example.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    routing {
        statusRoute()
        userRoutes()
        appointmentRoutes()
        slotRoutes()
    }
}