package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.office_hours_client.databinding.ActivityConcreteSlotBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.extensions.formatDate
import com.example.office_hours_client.extensions.formatTimeRange
import com.example.office_hours_client.models.*
import com.example.office_hours_client.plugins.api_root
import com.example.office_hours_client.plugins.appointment_path
import com.example.office_hours_client.plugins.client
import com.example.office_hours_client.plugins.networkScope
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConcreteSlotActivity : BackButtonActivity() {
    lateinit var binding: ActivityConcreteSlotBinding

    lateinit var concreteData: ConcreteSlotData
    lateinit var doctorData: UserPublicData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConcreteSlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (MainActivity.login?.user?.role != UserRole.Student) {
            binding.concreteReserveButton.isVisible = false
        }

        val concreteJson = intent.getStringExtra("concreteData")
        val doctorJson = intent.getStringExtra("doctorData")

        doctorJson ?: return finish()
        concreteJson ?: return finish()

        val concreteData = Json.decodeFromString<ConcreteSlotData>(concreteJson)
        val doctorData = Json.decodeFromString<UserPublicData>(doctorJson)
        this.concreteData = concreteData
        this.doctorData = doctorData

        updateViewState()

        binding.concreteReserveButton.setOnClickListener {
            reserveConcrete()
        }
    }

    fun reserveConcrete() {
        networkScope {
            val res = client.post("$api_root$appointment_path") {
                MainActivity.login?.also { bearerAuth(it.token) }
                contentType(ContentType.Application.Json)
                setBody(CreateAppointmentRequest(
                    doctor = doctorData.id,
                    startDateTime = concreteData.startDateTime,
                    endDateTime = concreteData.endDateTime
                ))
            }
            if (res.status == HttpStatusCode.Created) {
                val body = res.body<AppointmentData>()
//                Toast.makeText(this@SlotActivity, message, Toast.LENGTH_LONG).show()
                Intent(this@ConcreteSlotActivity, AppointmentActivity::class.java).apply {
                    putExtra("appointmentData", Json.encodeToString(body))
                    putExtra("isOtherKnown", true)
                    startActivity(this)
                }
                finish()
            } else {
                Toast.makeText(this@ConcreteSlotActivity, res.status.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateViewState() {
        val startDateTime = concreteData.startDateTime
        val endDateTime = concreteData.endDateTime

        val startTime = startDateTime.toJavaLocalDateTime().toLocalTime()
        val endTime = endDateTime.toJavaLocalDateTime().toLocalTime()
        val date = startDateTime.date

        binding.concreteTimeText.text = formatTimeRange(startTime, endTime)
        binding.concreteDayText.text = formatDate(date)
    }
}