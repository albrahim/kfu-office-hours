package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.office_hours_client.adapters.ConcreteSlotDataAdapter
import com.example.office_hours_client.databinding.ActivityDoctorBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.ConcreteSlotData
import com.example.office_hours_client.models.UserPublicData
import com.example.office_hours_client.models.UserRole
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoctorActivity : BackButtonActivity() {
    lateinit var binding: ActivityDoctorBinding

    var doctorData: UserPublicData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonString = intent.getStringExtra("doctorData")
        val _idMaybe = intent.getIntExtra("doctorId", -1)
        val extraId = if (_idMaybe == -1) null else _idMaybe
        extraId ?: jsonString ?: return finish()

        jsonString?.also {
            val doctorData = Json.decodeFromString<UserPublicData>(jsonString)
            this.doctorData = doctorData
            updateViewState()
        }

        if (doctorData?.role == UserRole.Doctor) {
            binding.concreteSlotsList.setOnItemClickListener { parent: AdapterView<*>, item: View, index: Int, _: Long ->
                (parent.getItemAtPosition(index) as ConcreteSlotData).also { concreteData ->
                    Intent(this, ConcreteSlotActivity::class.java).apply {
                        putExtra("concreteData", Json.encodeToString(concreteData))
                        putExtra("doctorData", Json.encodeToString(doctorData))
                        startActivity(this)
                    }
                }
            }
        }

        networkScope {
            extraId?.also {
                val res = client.get("$api_root$user_path/$extraId") {
                    MainActivity.login?.also { bearerAuth(it.token) }
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<UserPublicData>()
                    this@DoctorActivity.doctorData = body
                    updateViewState()
                } else {
                    Toast.makeText(this@DoctorActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateViewState()
    }

    private fun updateViewState() {
        doctorData?.also { doctorData ->
            binding.doctorName.text = "${doctorData.firstName} ${doctorData.lastName}"
        }
        if (doctorData?.role == UserRole.Doctor) {
            supportActionBar?.title = "Doctor Info"
            loadAppointmentList()
        }
        if (doctorData?.role == UserRole.Student) {
            supportActionBar?.title = "Student Info"
        }
    }

    fun loadAppointmentList() {
        doctorData?.id?.also { doctorId ->
            MainActivity.login?.token?.also { token ->
                networkScope {
                    val res = client.get("$api_root$slot_path/$doctorId") {
                        bearerAuth(token)
                    }
                    if (res.status == HttpStatusCode.OK) {
                        val body = res.body<List<ConcreteSlotData>>()
                        val adapter = ConcreteSlotDataAdapter(this@DoctorActivity, body)
                        binding.concreteSlotsList.adapter = adapter
                        binding.doctorSlotsEmptyText.visibility = if (body.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Toast.makeText(this@DoctorActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}