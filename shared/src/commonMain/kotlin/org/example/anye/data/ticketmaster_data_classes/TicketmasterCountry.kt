package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterCountry(
    val name: String? = null, // z.B. "Germany"
    val countryCode: String? = null // z.B. "DE"
)
