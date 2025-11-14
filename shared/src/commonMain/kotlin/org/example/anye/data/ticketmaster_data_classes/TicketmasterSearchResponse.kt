package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterSearchResponse(
    val _embedded: EmbeddedEvents? = null,
    val page: PageInfo
)

