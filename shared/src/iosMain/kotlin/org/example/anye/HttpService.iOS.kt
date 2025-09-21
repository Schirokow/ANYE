package org.example.anye

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import platform.Foundation.NSLog

actual val httpClient: HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                logMessage("HTTP: $message")
            }
        }
        level = LogLevel.ALL
    }
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}

actual fun logMessage(message: String) {
    NSLog("HttpService: %s", message)
}