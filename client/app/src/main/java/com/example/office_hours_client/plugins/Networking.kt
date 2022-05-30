package com.example.office_hours_client.plugins

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.ignoreIoExceptions
import okio.IOException
import java.net.ConnectException


val client = HttpClient(OkHttp) {

    engine {
        config {
            followRedirects(true)
        }
    }
    install(ContentNegotiation) {
        json()
    }
}

const val api_root = "http://10.0.2.2:8080/"
const val user_path = "user"
const val slot_path = "slot"
const val appointment_path = "appointment"

fun ComponentActivity.networkScope(
    onFail: (() -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        try {
            block()
        } catch (e: ConnectException) {
            println(e.localizedMessage)
            if (onFail == null) {
                Toast.makeText(baseContext, "Connection Failure", Toast.LENGTH_LONG).show()
            } else {
                onFail()
            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(baseContext, "Failure", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(baseContext, "Failure", Toast.LENGTH_LONG).show()
        }
    }
}