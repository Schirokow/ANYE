package org.example.anye

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform