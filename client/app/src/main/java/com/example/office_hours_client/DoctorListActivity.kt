package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.office_hours_client.adapters.DoctorDataAdapter
import com.example.office_hours_client.databinding.ActivityDoctorListBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.UserPublicData
import com.example.office_hours_client.models.UserRole
import com.example.office_hours_client.plugins.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DoctorListActivity : BackButtonActivity() {
    lateinit var binding: ActivityDoctorListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.doctorList.setOnItemClickListener { parent: AdapterView<*>, item: View, index: Int, _: Long ->
            (parent.getItemAtPosition(index) as UserPublicData).also { doctorData ->
                Intent(this, DoctorActivity::class.java).apply {
                    putExtra("doctorData", Json.encodeToString(doctorData))
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity.login?.token?.also { token ->
            networkScope {
                val res = client.get("$api_root$user_path") {
                    bearerAuth(token)
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<List<UserPublicData>>().filter { it.role == UserRole.Doctor }
                    val adapter = DoctorDataAdapter(this@DoctorListActivity, body)
                    binding.doctorList.adapter = adapter

                    binding.doctorEmptyText.visibility = if (body.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@DoctorListActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}