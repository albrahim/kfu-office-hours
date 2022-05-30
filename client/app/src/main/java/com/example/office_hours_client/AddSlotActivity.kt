package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.office_hours_client.databinding.ActivityAddSlotBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.CreateSlotConflictResponse
import com.example.office_hours_client.models.CreateSlotRequest
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.*
import java.time.LocalTime
import java.time.ZonedDateTime

class AddSlotActivity : BackButtonActivity() {
    lateinit var binding: ActivityAddSlotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmAddSlot.setOnClickListener {
            binding.confirmAddSlot.isEnabled = false
            val localTimeStart = LocalTime.of(binding.startTimePicker.hour, binding.startTimePicker.minute)
            val localTimeEnd = LocalTime.of(binding.endTimePicker.hour, binding.endTimePicker.minute)

            val millisPerSecond = 1000
            val milliStart = localTimeStart.toSecondOfDay() * millisPerSecond
            val milliEnd = localTimeEnd.toSecondOfDay() * millisPerSecond

            val selectedDay = binding.dayPicker.value.let { DayOfWeek.values()[it] }
            networkScope {
                val res = client.post("$api_root$slot_path") {
//                    Toast.makeText(this@AddSlotActivity, "locstart: $localTimeStart locend: $localTimeEnd start: $milliStart end: $milliEnd", Toast.LENGTH_LONG).show()
                    val body = CreateSlotRequest(
                        startNano = milliStart,
                        endNano = milliEnd,
                        dayOfWeek = selectedDay
                    )
                    contentType(ContentType.Application.Json)
                    MainActivity.login?.let { it1 -> bearerAuth(it1.token) }
                    setBody(body)
                }

                if (res.status == HttpStatusCode.Created) {
//                    val message = res.body<String>()
//                    Toast.makeText(this@AddSlotActivity, message, Toast.LENGTH_LONG).show()
                    finish()
                } else if (res.status == HttpStatusCode.Conflict) {
                    val body = res.body<CreateSlotConflictResponse>()
                    val message = body.fail
                    val conflictingSlotId = body.conflictingSlot
                    AlertDialog.Builder(this@AddSlotActivity).apply {
                        setCancelable(true)
                        setTitle("Conflict")
                        setMessage("You have another office hour that conflicts with this one")
                        setPositiveButton("Dismiss") { dialog, _ ->
                            dialog.cancel()
                        }
                        setNegativeButton("See Conflict") { dialog, _ ->
                            val intent = Intent(
                                context,
                                SlotActivity::class.java
                            )
//                            Toast.makeText(this@AddSlotActivity, conflictingSlotId, Toast.LENGTH_LONG).show()
                            intent.putExtra("slotId", conflictingSlotId)
                            startActivity(intent)
                        }
                    }.create().show()
                } else {
                    Toast.makeText(this@AddSlotActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                }
                binding.confirmAddSlot.isEnabled = true
            }
        }

        val dayArray = DayOfWeek.values().map { it.toString() }.toTypedArray()

        binding.dayPicker.minValue = 0
        binding.dayPicker.maxValue = dayArray.lastIndex
        binding.dayPicker.displayedValues = dayArray

        MainActivity.status?.zone?.also { zone ->
            val currentDateTime = ZonedDateTime.now(zone.toJavaZoneId())

            var currentTime = currentDateTime.toLocalTime()
            var nextTime = currentTime.plusMinutes(30)
            val currentDay = currentDateTime.dayOfWeek

            if (nextTime < currentTime) {
                currentTime = LocalTime.MAX.minusMinutes(30)
                nextTime = LocalTime.MAX
            }


            binding.startTimePicker.hour = currentTime.hour
            binding.startTimePicker.minute = currentTime.minute
            binding.endTimePicker.hour = nextTime.hour
            binding.endTimePicker.minute = nextTime.minute
            binding.dayPicker.value = dayArray.indexOf(currentDay.toString())

            binding.startTimePicker.setOnTimeChangedListener { _, _, _ ->
                val startLocalTime = LocalTime.of(
                    binding.startTimePicker.hour,
                    binding.startTimePicker.minute
                )
                val endLocalTime = LocalTime.of(
                    binding.endTimePicker.hour,
                    binding.endTimePicker.minute
                )
                if (!startLocalTime.isBefore(endLocalTime)) {
                    var newEndTime = startLocalTime.plusMinutes(30)
                    if (newEndTime < startLocalTime) {
                        newEndTime = LocalTime.MAX
                    }
                    binding.endTimePicker.hour = newEndTime.hour
                    binding.endTimePicker.minute = newEndTime.minute
                }
            }

            binding.endTimePicker.setOnTimeChangedListener { _, _, _ ->
                val startLocalTime = LocalTime.of(
                    binding.startTimePicker.hour,
                    binding.startTimePicker.minute
                )
                val endLocalTime = LocalTime.of(
                    binding.endTimePicker.hour,
                    binding.endTimePicker.minute
                )
                if (!startLocalTime.isBefore(endLocalTime)) {
                    var newStartTime = endLocalTime.minusMinutes(30)
                    if (newStartTime > endLocalTime) {
                        newStartTime = LocalTime.MIN
                    }
                    binding.startTimePicker.hour = newStartTime.hour
                    binding.startTimePicker.minute = newStartTime.minute
                }
            }
        }
    }
}