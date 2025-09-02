package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.EventByIdImplFlow
import org.example.anye.data.TicketmasterEvent

class GetEventByIdUseCase {
    private val event = EventByIdImplFlow()
    fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?> {
        return event.getEventByIdFlow(eventId)
    }
}