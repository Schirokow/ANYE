package org.example.anye

import android.util.Log

actual fun logMessage(message: String) {
    Log.d("HttpService", message)
}