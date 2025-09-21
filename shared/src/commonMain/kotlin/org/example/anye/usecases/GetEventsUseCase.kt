package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.EventsRepository
import org.example.anye.data.TicketmasterEvent

class GetEventsUseCase(private val events: EventsRepository) {
    fun getEventsFlow(city: String): Flow<List<TicketmasterEvent>> {
        return events.getEventsDataFlow(city)
    }
}