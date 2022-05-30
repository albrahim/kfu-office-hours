package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.office_hours_client.databinding.ActivityAppointmentBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.extensions.formatDate
import com.example.office_hours_client.extensions.formatTimeRange
import com.example.office_hours_client.models.AppointmentData
import com.example.office_hours_client.models.UserRole
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AppointmentActivity : BackButtonActivity() {
    lateinit var binding: ActivityAppointmentBinding
    lateinit var appointmentData: AppointmentData
    var isOtherKnown: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteAppointmentButton.setOnClickListener { deleteAppointment() }
        binding.otherUserButton.visibility = View.GONE

        val jsonString = intent.getStringExtra("appointmentData")
        val idString = intent.getStringExtra("appointmentId")
        isOtherKnown = intent.getBooleanExtra("isOtherKnown", false)
        idString ?: jsonString ?: return finish()

        jsonString?.also {
            val appointmentData = Json.decodeFromString<AppointmentData>(jsonString)
            this.appointmentData = appointmentData
            updateViewState()
        }

        if (!isOtherKnown) { // if appointment viewed by student
            if (MainActivity.login?.user?.role == UserRole.Student) {
                binding.otherUserButton.text = "Doctor"
                binding.otherUserButton.setOnClickListener {
                    Intent(this, DoctorActivity::class.java).apply {
                        putExtra("doctorId", appointmentData.doctor)
                        startActivity(this)
                    }
                }
                binding.otherUserButton.visibility = View.VISIBLE
            } else if (MainActivity.login?.user?.role == UserRole.Doctor) { // if appointment viewed by doctor
                binding.otherUserButton.text = "Student"
                binding.otherUserButton.setOnClickListener {
                    Intent(this, DoctorActivity::class.java).apply {
                        putExtra("doctorId", appointmentData.student)
                        startActivity(this)
                    }
                }
                binding.otherUserButton.visibility = View.VISIBLE
            }
        }

        networkScope {
            idString?.also {
                val res = client.get("$api_root$appointment_path") {
                    MainActivity.login?.also { bearerAuth(it.token) }
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<List<AppointmentData>>().filter { it.id == idString }
                    val appointmentData = body.find { it.id == idString }
                    appointmentData?.also {
                        this@AppointmentActivity.appointmentData = appointmentData
                        updateViewState()
                    }
                } else {
                    Toast.makeText(this@AppointmentActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    fun deleteAppointment() {
        val id = appointmentData.id

        networkScope {
            val res = client.delete("$api_root$appointment_path/$id") {
                MainActivity.login?.also { bearerAuth(it.token) }
            }
            if (res.status == HttpStatusCode.OK) {
//                val message = res.body<String>()
//                Toast.makeText(this@SlotActivity, message, Toast.LENGTH_LONG).show()
                finish()
            } else if (res.status == HttpStatusCode.NotFound) {
                finish()
            } else {
                Toast.makeText(this@AppointmentActivity, res.status.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateViewState() {
        val startDateTime = appointmentData.startDateTime
        val endDateTime = appointmentData.endDateTime

        val startTime = startDateTime.toJavaLocalDateTime().toLocalTime()
        val endTime = endDateTime.toJavaLocalDateTime().toLocalTime()

        binding.appointmentTimeText.text = formatTimeRange(startTime, endTime)
        binding.appointmentDayText.text = formatDate(startDateTime.date)
    }
}