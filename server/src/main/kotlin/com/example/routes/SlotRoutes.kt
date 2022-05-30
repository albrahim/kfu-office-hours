package com.example.routes

import com.example.DatabaseFactory.dbQuery
import com.example.extensions.*
import com.example.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import java.time.LocalTime

fun Route.slotRoutes() {
    route("/slot") {
        authenticate {
            createAuthedUserSlot()
            getAuthedUserSlots()
            deleteAuthedUserSlot()

            getDoctorSlots()
        }
    }
}

fun Route.createAuthedUserSlot() {
    post {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        val (startTime, endTime, dayOfWeek) = call.receive<CreateSlotRequest>()

        if (startTime !in dayRange || endTime !in dayRange) return@post call.response.status(HttpStatusCode.BadRequest)
        if (startTime >= endTime) return@post call.response.status(HttpStatusCode.BadRequest)

        dbQuery {
            User.findById(id)
                ?.also { doctor ->
                    if (doctor.role != UserRole.Doctor) return@dbQuery call.response.status(HttpStatusCode.Unauthorized)

                    Slot.find(Slots.doctor eq doctor.id)
                        .find {
                            val innerOverlap = startTime in it.startNano..it.endNano || endTime in it.startNano..it.endNano
                            val outerOverlap = it.startNano in startTime..endTime || it.endNano in startTime..endTime
                            (dayOfWeek == it.dayOfWeek) && (innerOverlap || outerOverlap)
                        }.also {
                            if (it != null) {
                                return@dbQuery call.respond(HttpStatusCode.Conflict, CreateSlotConflictResponse(conflictingSlot = it.data.id))
                            }
                            if (it == null) {
                                Slot.new {
                                    this.doctor = doctor.id
                                    this.startNano = startTime
                                    this.endNano = endTime
                                    this.dayOfWeek = dayOfWeek
                                }.also { slot ->
                                    return@dbQuery call.respond(HttpStatusCode.Created, slot.data)
                                }
                            }
                        }
                }
        }
    }
}

fun Route.getAuthedUserSlots() {
    get {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        dbQuery {
            User.findById(id)
                ?.also { doctor ->
                    Slot.find{ Slots.doctor eq doctor.id }
                        .map{ it.data }
                        .let(::orderSlotData)
                        .also {
                            call.respond(it)
                        }
                }
        }
    }
}

fun Route.deleteAuthedUserSlot() {
    delete("{slotId?}") {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        val slotId = call.parameters["slotId"] ?: return@delete

        dbQuery {
            Slot
                .find { (Slots.doctor eq id) }
                .findLast { it.id.value.toString() == slotId }
                ?.also { slot ->
                    val slotLocalTime = LocalTime.ofNanoOfDay(slot.startNano * milliToNano)
                    Appointment.find { (Appointments.doctor eq id) }.also { doctorAppointments ->
                        doctorAppointments.filter {
                            val appointmentLocalTime = it.startDateTime.toLocalDateTime().toJavaLocalDateTime().toLocalTime()
                            appointmentLocalTime == slotLocalTime
                        }.forEach {
                            it.delete()
                        }
                        slot.delete()
                        call.respondText("Slot removed successfully")
                    }
                }
        }
    }
}

fun Route.getDoctorSlots() {
    get("{doctorId?}") {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        val doctorId = call.parameters["doctorId"]?.toIntOrNull() ?: return@get

        dbQuery {
            val now = clock.now().local
            val nowDate = now.date.toJavaLocalDate()
            val upcomingDates = (0..13L).map { nowDate.plusDays(it) }

            User.findById(id)?.also { user ->
                User.findById(doctorId)?.also { doctor ->
                    Slot.find{ Slots.doctor eq doctor.id }
                        .map{ it.data }
                        .let(::concreteSlotsFromSlotData)
                        .also { concreteSlotData ->
                            val startDatesList = concreteSlotData.map { it.startDateTime.toString() }
                            Appointment.find((Appointments.startDateTime inList startDatesList) and (Appointments.doctor eq doctorId))
                                .also {
                                    val conflictingStartTimes = it.map { it.startDateTime }
                                    val cleanList = concreteSlotData.filter { it.startDateTime.toString() !in conflictingStartTimes }
                                    call.respond(cleanList)
                                }
                        }
                }
            }
        }
    }
}