package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddedEvents(
    val events: List<TicketmasterEvent> = emptyList()
)
