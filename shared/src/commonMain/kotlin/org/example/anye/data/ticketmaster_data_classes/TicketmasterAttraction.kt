package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterAttraction(
    val id: String,
    val name: String,
    val url: String,
    val images: List<TicketmasterImage>? = null
)

