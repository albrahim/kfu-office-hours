package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        DatabaseFactory.init()
        configureSecurity()
        configureStatus()
        configureSerialization()
        configureRouting()
    }.start(wait = true)
}
