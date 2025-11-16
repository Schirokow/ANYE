package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterAddress(
    val line1: String? = null // z.B. "Mercedes Platz 1"
)
