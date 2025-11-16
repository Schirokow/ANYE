package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterVenue(
    val id: String,
    val name: String? = null,
    val address: TicketmasterAddress? = null,
    val city: TicketmasterCity? = null,
    val state: TicketmasterState? = null,
    val country: TicketmasterCountry? = null,
    val location: TicketmasterLocation? = null
)
