package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.office_hours_client.MainActivity.Companion.login
import com.example.office_hours_client.adapters.SlotDataAdapter
import com.example.office_hours_client.databinding.ActivitySlotListBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.SlotData
import com.example.office_hours_client.plugins.api_root
import com.example.office_hours_client.plugins.client
import com.example.office_hours_client.plugins.networkScope
import com.example.office_hours_client.plugins.slot_path
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Clock

class SlotListActivity : BackButtonActivity() {
    lateinit var binding: ActivitySlotListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlotListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.newSlotButton.isEnabled = false
        binding.newSlotButton.setOnClickListener {
            Intent(this, AddSlotActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.slotList.setOnItemClickListener { parent: AdapterView<*>, item: View, index: Int, _: Long ->
            (parent.getItemAtPosition(index) as SlotData).also { slotData ->
                Intent(this, SlotActivity::class.java).apply {
                    putExtra("slotData", Json.encodeToString(slotData))
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        login?.token?.also { token ->
            networkScope {
                val res = client.get("$api_root$slot_path") {
                    bearerAuth(token)
                }
                if (res.status == HttpStatusCode.OK) {
                    val body = res.body<List<SlotData>>()
                    val adapter = SlotDataAdapter(this@SlotListActivity, body)
                    binding.slotList.adapter = adapter
                    binding.newSlotButton.isEnabled = true

                    binding.slotEmptyText.visibility = if (body.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@SlotListActivity, res.status.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}