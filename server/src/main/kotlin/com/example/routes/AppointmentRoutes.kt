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
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

fun Route.appointmentRoutes() {
    route("/appointment") {
        authenticate {
            createAuthedUserAppointment()
            getAuthedUserAppointments()
            deleteAuthedUserAppointment()
        }
    }
}

fun Route.createAuthedUserAppointment() {
    post {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        val studentId = id
        val (doctorId, startDateTime, endDateTime) = call.receive<CreateAppointmentRequest>()

        if (startDateTime >= endDateTime) return@post call.response.status(HttpStatusCode.BadRequest)

        dbQuery {
            User.findById(studentId)
                ?.also { student ->
                    if (student.role != UserRole.Student) return@dbQuery call.response.status(HttpStatusCode.Unauthorized)

                    User.findById(doctorId)
                        ?.also { doctor ->
                            if (doctor.role != UserRole.Doctor) return@dbQuery call.response.status(HttpStatusCode.BadRequest)

                            Slot.find(Slots.doctor eq doctor.id)
                                .map { it.data }
                                .let(::concreteSlotsFromSlotData)
                                .map{ Pair(it.startDateTime, it.endDateTime) }
                                .also { acceptableTimePairs ->
                                    val requestDateTimePair = Pair(startDateTime, endDateTime)
                                    if (requestDateTimePair in acceptableTimePairs) {
                                        Appointment.findOne((Appointments.startDateTime eq startDateTime.toString()) and ((Appointments.doctor eq doctorId) or (Appointments.student eq studentId)))
                                            .also {
                                                if (it == null) {
                                                    Appointment.new {
                                                        this.student = student.id
                                                        this.doctor = doctor.id
                                                        this.startDateTime = startDateTime.toString()
                                                        this.endDateTime = endDateTime.toString()
                                                    }.also { appointment ->
                                                        call.respond(HttpStatusCode.Created, appointment.data)
                                                    }
                                                } else {
                                                    call.response.status(HttpStatusCode.Conflict)
                                                    call.respondText("Date-time outside free slot")
                                                }
                                            }
                                    } else {
                                        call.response.status(HttpStatusCode.NotAcceptable)
                                        call.respondText("Date-time outside slot")
                                    }
                                }
                        }
                }
        }
    }
}

fun Route.getAuthedUserAppointments() {
    get {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()
        dbQuery {
            val minimumShownTime = clock.now().toLocalDateTime(zone).toJavaLocalDateTime().minusHours(2).toKotlinLocalDateTime()
            Appointment
                .find{ (Appointments.student eq id) or (Appointments.doctor eq id) }
                .map{ it.data }.filter { it.endDateTime > minimumShownTime }
                .also {
                    call.respond(it)
                }
        }
    }
}

fun Route.deleteAuthedUserAppointment() {
    delete("{appointmentId?}") {
        val principal = call.principal<JWTPrincipal>()
        val id = principal!!.payload.getClaim("userid").asInt()

        val appointmentId = call.parameters["appointmentId"] ?: return@delete

        dbQuery {
            Appointment
                .find { (Appointments.student eq id) or (Appointments.doctor eq id) }
                .findLast { it.id.value.toString() == appointmentId }
                ?.also {
                    it.delete()
                    call.respondText("Appointment removed successfully")
                }
        }
    }
}