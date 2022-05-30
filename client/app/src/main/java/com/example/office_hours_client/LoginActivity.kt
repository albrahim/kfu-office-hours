package com.example.office_hours_client

import android.os.Bundle
import android.widget.Toast
import com.example.office_hours_client.databinding.ActivityLoginBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.extensions.invoke
import com.example.office_hours_client.models.*
import com.example.office_hours_client.plugins.api_root
import com.example.office_hours_client.plugins.client
import com.example.office_hours_client.plugins.networkScope
import com.example.office_hours_client.plugins.user_path
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class LoginActivity : BackButtonActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginCancelButton { finish() }
        binding.loginConfirmButton.setOnClickListener {
            networkScope {
                client.post("$api_root$user_path/signin") {
                    UserSignInRequest(
                        username = binding.usernameEdit.text.toString(),
                        password = binding.passwordEdit.text.toString(),
                    ).also {
                        contentType(ContentType.Application.Json)
                        setBody(it)
                    }
                }
                    .also {
                        if (it.status == HttpStatusCode.OK) {
                            val response = it.body<UserSignInResponseSuccess>()
                            MainActivity.login = response
                            getSharedPreferences("Authentication", MODE_PRIVATE).edit().apply {
                                putString("token", response.token)
                                apply()
                            }
                            finish()
                        } else if (it.status == HttpStatusCode.NotFound) {
                            val response = it.body<UserSignInResponseFail>()
                            Toast.makeText(this@LoginActivity, response.fail, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@LoginActivity, it.status.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}