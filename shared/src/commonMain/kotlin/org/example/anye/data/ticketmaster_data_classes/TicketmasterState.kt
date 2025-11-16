package org.example.anye.data.ticketmaster_data_classes

import kotlinx.serialization.Serializable

@Serializable
data class TicketmasterState(
    val name: String? = null, // z.B. "Berlin" (in DE, kann auch leer sein)
    val stateCode: String? = null // z.B. "BE"
)
