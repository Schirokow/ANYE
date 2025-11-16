package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterImage(
    val ratio: String? = null,
    val url: String,
    val width: Int,
    val height: Int,
    val fallback: Boolean
)
