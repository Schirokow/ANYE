package org.example.anye.business.usecases

import org.example.anye.data.EventByIdImplFlow
import org.example.anye.data.TicketmasterEvent
import kotlinx.coroutines.flow.Flow

class GetEventByIdUseCase {
    private val event = EventByIdImplFlow()
    fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?> {
        return event.getEventByIdFlow(eventId)
    }
}