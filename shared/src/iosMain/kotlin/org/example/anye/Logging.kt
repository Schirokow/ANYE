package org.example.anye

import platform.Foundation.NSLog

actual fun logMessage(message: String) {
    NSLog("HttpService: %s", message)
}