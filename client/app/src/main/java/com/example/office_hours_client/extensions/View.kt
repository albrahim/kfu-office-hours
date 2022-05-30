package com.example.office_hours_client.extensions

import android.view.View
import android.widget.Button

operator fun <T: Button> T.invoke(callback: (View) -> Unit) {
    this.setOnClickListener(callback)
}