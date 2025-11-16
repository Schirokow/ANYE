package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterEvent(
    val id: String,
    val name: String,
    val type: String, // event, attraction, venue
    val url: String,
    val locale: String,
    val images: List<TicketmasterImage>? = null,
    val dates: TicketmasterDates? = null,
    val _embedded: TicketmasterEventEmbedded? = null // Für Venue, Attractions etc.
)
