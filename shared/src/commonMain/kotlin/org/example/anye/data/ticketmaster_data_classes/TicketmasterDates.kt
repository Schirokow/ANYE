package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterDates(
    val start: TicketmasterStartDates? = null
)
