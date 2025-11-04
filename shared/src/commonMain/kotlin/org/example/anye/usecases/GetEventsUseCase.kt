package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.EventsRepository
import org.example.anye.data.TicketmasterEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GetEventsUseCase(): KoinComponent {
    private val events: EventsRepository by inject()
    fun getEventsFlow(city: String): Flow<List<TicketmasterEvent>> {
        return events.getEventsDataFlow(city)
    }
}