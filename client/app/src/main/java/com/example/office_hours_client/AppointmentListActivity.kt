package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.office_hours_client.adapters.AppointmentDataAdapter
import com.example.office_hours_client.databinding.ActivityAppointmentListBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.AppointmentData
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppointmentListActivity : BackButtonActivity() {
    lateinit var binding: ActivityAppointmentListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appointmentList.setOnItemClickListener { parent: AdapterView<*>, item: View, index: Int, _: Long ->
            (parent.getItemAtPosition(index) as AppointmentData).also { appointmentData ->
                Intent(this, AppointmentActivity::class.java).apply {
                    putExtra("appointmentData", Json.encodeToString(appointmentData))
                    putExtra("isOtherKnown", false)
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity.login?.token?.also { token ->
            networkScope {
                val res = client.get("$api_root$appointment_path") {
                    bearerAuth(token)
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<List<AppointmentData>>()
                    val adapter = AppointmentDataAdapter(this@AppointmentListActivity, body)
                    binding.appointmentList.adapter = adapter

                    binding.appointmentEmptyText.visibility = if (body.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@AppointmentListActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}