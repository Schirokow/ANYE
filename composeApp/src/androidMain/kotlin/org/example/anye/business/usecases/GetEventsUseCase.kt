package org.example.anye.business.usecases

import org.example.anye.data.EventsRepositoryImpl
import org.example.anye.data.TicketmasterEvent
import kotlinx.coroutines.flow.Flow

class GetEventsUseCase() {
        private val events = EventsRepositoryImpl()
    fun getEventsFlow(city: String): Flow<List<TicketmasterEvent>> {
        return events.getEventsDataFlow(city)
    }
}