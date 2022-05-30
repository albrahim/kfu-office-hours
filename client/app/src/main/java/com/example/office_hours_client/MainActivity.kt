package com.example.office_hours_client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import com.example.office_hours_client.databinding.ActivityMainBinding
import com.example.office_hours_client.extensions.BackButtonActivity
import com.example.office_hours_client.models.StatusResponse
import com.example.office_hours_client.plugins.api_root
import com.example.office_hours_client.plugins.client
import com.example.office_hours_client.extensions.invoke
import com.example.office_hours_client.models.UserRole
import com.example.office_hours_client.models.UserSignInResponseSuccess
import com.example.office_hours_client.plugins.networkScope
import com.example.office_hours_client.plugins.user_path
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class MainActivity : BackButtonActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var loggedInButtons: List<Button>
    lateinit var loggedOutButtons: List<Button>
    lateinit var doctorButtons: List<Button>

    val allButtons: List<Button> get() = (loggedOutButtons + loggedInButtons + doctorButtons)

    companion object {
        var status: StatusResponse? = null
        var login: UserSignInResponseSuccess? = null
    }

    var login get() = Companion.login
    set(value) {
        Companion.login = value
        if (value == null) clearLoginPreferences()
        updateLoginViewState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loggedOutButtons = listOf(
            binding.signupButton,
            binding.loginButton
        )
        loggedInButtons = listOf(
            binding.logoutButton,
            binding.doctorsButton,
            binding.appointmentsButton,
            binding.profileButton
        )
        doctorButtons = listOf(
            binding.slotButton,
        )

        allButtons.forEach {
            it.visibility = View.GONE
            it.isEnabled = false
        }

        binding.retryButton.setOnClickListener {
            establishConnection()
            binding.retryButton.isEnabled = false
        }

        binding.signupButton {
            Intent(this@MainActivity, SignupActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.loginButton {
            Intent(this@MainActivity, LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.slotButton {
            Intent(this@MainActivity, SlotListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.doctorsButton {
            Intent(this@MainActivity, DoctorListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.appointmentsButton {
            Intent(this@MainActivity, AppointmentListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.profileButton {
            Intent(this@MainActivity, MyProfileActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.logoutButton {
            login = null
        }

        establishConnection()
    }

    override fun onResume() {
        super.onResume()
        allButtons.forEach {
            it.visibility = View.GONE
        }
        updateLoginViewState()
    }


    private fun establishConnection() {
        val onFail = {
            binding.buttonContainer.isVisible = false
            binding.statusText.text = "Failed to connect to the server"
            binding.statusContainer.isVisible = true
            binding.retryButton.isEnabled = true
        }

        networkScope(onFail = onFail) {
            val res = client.get("$api_root")
            if (res.status == HttpStatusCode.OK) {
                status = res.body()
                binding.statusContainer.isVisible = false
                binding.buttonContainer.isVisible = true

                val prefs = getSharedPreferences("Authentication", MODE_PRIVATE)
                val token = prefs.getString("token", null)
                token?.also {
                    val res = client.get("$api_root$user_path/renew-token") { bearerAuth(token) }
                    if (res.status == HttpStatusCode.OK) {
                        login = res.body()
                    } else if (res.status == HttpStatusCode.NotFound) {
                        clearLoginPreferences()
                    }
                }
                allButtons.forEach {
                    it.isEnabled = true
                }
            }
        }
    }

    fun updateLoginViewState() {
        if (login != null) onLoggedIn() else onLoggedOut()
    }

    fun onLoggedIn() {
        login?.also { login ->
            loggedOutButtons.forEach { it.visibility = View.GONE }
            loggedInButtons.forEach { it.visibility = View.VISIBLE }
            if (login.user.role == UserRole.Doctor) {
                doctorButtons.forEach { it.visibility = View.VISIBLE }
            }
        }
    }

    fun onLoggedOut() {
        login ?: run {
            loggedOutButtons.forEach { it.visibility = View.VISIBLE }
            loggedInButtons.forEach { it.visibility = View.GONE }
            doctorButtons.forEach { it.visibility = View.GONE }
        }
    }

    private fun clearLoginPreferences() {
        getSharedPreferences("Authentication", MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }
}