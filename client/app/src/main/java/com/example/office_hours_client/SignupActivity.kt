package com.example.office_hours_client

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.office_hours_client.databinding.ActivitySignupBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.extensions.invoke
import com.example.office_hours_client.models.UserRole
import com.example.office_hours_client.models.UserSignInResponseSuccess
import com.example.office_hours_client.models.UserSignupRequest
import com.example.office_hours_client.plugins.api_root
import com.example.office_hours_client.plugins.client
import com.example.office_hours_client.plugins.networkScope
import com.example.office_hours_client.plugins.user_path
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class SignupActivity : BackButtonActivity() {
    lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.roleSpinner.apply {
            val adapter = ArrayAdapter<UserRole>(
                this@SignupActivity,
                android.R.layout.simple_spinner_dropdown_item,
                UserRole.values().reversedArray()
            )
            this.adapter = adapter
        }

        binding.signupCancelButton { finish() }
        binding.signupConfirmButton.setOnClickListener {
            networkScope {
                client.post("$api_root$user_path") {
                    UserSignupRequest(
                        username = binding.usernameEdit.text.toString(),
                        email = binding.emailEdit.text.toString(),
                        password = binding.passwordEdit.text.toString(),
                        firstName = binding.firstNameEdit.text.toString(),
                        lastName = binding.lastNameEdit.text.toString(),
                        role = binding.roleSpinner.selectedItem as UserRole,
                    ).also {
                        contentType(ContentType.Application.Json)
                        setBody(it)
                    }
                }
                    .also {
                        if (it.status == HttpStatusCode.Created) {
                            val body = it.body<UserSignInResponseSuccess>()
                            MainActivity.login = body
                            getSharedPreferences("Authentication", MODE_PRIVATE).edit().apply {
                                putString("token", body.token)
                                apply()
                            }
//                            Toast.makeText(this@SignupActivity, body.success, Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val message = it.body<String>()
                            Toast.makeText(this@SignupActivity, message, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}