package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterStartDates(
    val localDate: String? = null, // z.B. "2025-08-01"
    val localTime: String? = null, // z.B. "19:00:00"
    val dateTime: String? = null // z.B. "2025-08-01T19:00:00Z"
)
