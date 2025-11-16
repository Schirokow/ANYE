package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterLocation(
    val longitude: String? = null,
    val latitude: String? = null
)
