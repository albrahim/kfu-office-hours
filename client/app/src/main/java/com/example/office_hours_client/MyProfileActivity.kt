package com.example.office_hours_client

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.office_hours_client.databinding.ActivityMyProfileBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

class MyProfileActivity : BackButtonActivity() {
    lateinit var binding: ActivityMyProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainActivity.login?.user?.also { userData ->
            binding.fullNameText.text = "${userData.firstName} ${userData.lastName}"
        }

        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this@MyProfileActivity, R.style.DestructiveAlertDialogTheme).apply {
                setCancelable(false)
                setTitle("Delete Account")
                setMessage("Are you sure?")
                setPositiveButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton("Delete") { dialog, _ ->
                    deleteAccount()
                }
            }.create().show()
        }
    }

    fun deleteAccount() {
        networkScope {
            val res = client.delete("$api_root$user_path/me") {
                MainActivity.login?.also { bearerAuth(it.token) }
            }
            if (res.status == HttpStatusCode.Accepted) {
//                val message = res.body<String>()
//                Toast.makeText(this@SlotActivity, message, Toast.LENGTH_LONG).show()
                MainActivity.login = null
                finish()
            } else {
                Toast.makeText(this@MyProfileActivity, res.status.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}