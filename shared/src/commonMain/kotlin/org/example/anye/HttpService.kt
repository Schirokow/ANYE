package org.example.anye

import io.ktor.client.HttpClient


expect val httpClient: HttpClient

expect fun logMessage(message: String) // Plattformspezifisches Logging

