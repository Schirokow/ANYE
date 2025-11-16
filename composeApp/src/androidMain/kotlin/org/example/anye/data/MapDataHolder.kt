package org.example.anye.data

import org.example.anye.data.ticketmaster_data_classes.TicketmasterEvent

/**
 * Ein einfacher statischer "Holder", um eine Event-Liste
 * vom HomeScreen zum LocationScreen zu transportieren,
 * ohne sie durch die Navigations-Argumente zu zwingen.
 */
object MapDataHolder {
    var events: List<TicketmasterEvent> = emptyList()

    var shouldFollowUser: Boolean = true
}