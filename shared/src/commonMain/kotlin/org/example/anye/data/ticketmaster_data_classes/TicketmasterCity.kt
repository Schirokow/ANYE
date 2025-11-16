package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterCity(
    val name: String? = null // z.B. "Berlin"
)
