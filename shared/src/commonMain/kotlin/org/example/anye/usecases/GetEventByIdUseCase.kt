package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.EventsRepository
import org.example.anye.data.ticketmaster_data_classes.TicketmasterEvent

class GetEventByIdUseCase(private val event: EventsRepository) {
    fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?> {
        return event.getEventByIdFlow(eventId)
    }
}