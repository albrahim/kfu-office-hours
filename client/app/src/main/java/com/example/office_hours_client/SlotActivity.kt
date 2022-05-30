package com.example.office_hours_client

import android.os.Bundle
import android.widget.Toast
import com.example.office_hours_client.databinding.ActivitySlotBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.extensions.formatTimeRange
import com.example.office_hours_client.models.SlotData
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalTime

class SlotActivity : BackButtonActivity() {
    lateinit var binding: ActivitySlotBinding
    lateinit var slotData: SlotData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteSlotButton.setOnClickListener { deleteSlot() }

        val jsonString = intent.getStringExtra("slotData")
        val idString = intent.getStringExtra("slotId")
        idString ?: jsonString ?: return finish()

        jsonString?.also {
            val slotData = Json.decodeFromString<SlotData>(jsonString)
            this.slotData = slotData
            updateViewState()
        }

        networkScope {
            idString?.also {
                val res = client.get("$api_root$slot_path") {
                    MainActivity.login?.also { bearerAuth(it.token) }
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<List<SlotData>>()
                    val slotData = body.find { it.id == idString }
                    slotData?.also {
                        this@SlotActivity.slotData = slotData
                        updateViewState()
                    }
                } else {
                    Toast.makeText(this@SlotActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    fun deleteSlot() {
        val id = slotData.id

        networkScope {
            val res = client.delete("$api_root$slot_path/$id") {
                MainActivity.login?.also { bearerAuth(it.token) }
            }
            if (res.status == HttpStatusCode.OK) {
//                val message = res.body<String>()
//                Toast.makeText(this@SlotActivity, message, Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this@SlotActivity, res.status.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateViewState() {
        val milliToNano = 1000000L
        val startTime = LocalTime.ofNanoOfDay(slotData.startNano * milliToNano)
        val endTime = LocalTime.ofNanoOfDay(slotData.endNano * milliToNano)

        binding.slotTimeText.text = formatTimeRange(startTime, endTime)
        binding.slotDayText.text = slotData.dayOfWeek.toString()
    }
}