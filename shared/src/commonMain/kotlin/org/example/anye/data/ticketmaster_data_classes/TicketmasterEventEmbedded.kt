package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterEventEmbedded(
    val venues: List<TicketmasterVenue>? = null,
    val attractions: List<TicketmasterAttraction>? = null
)
