package org.example.anye.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface EventsRepository {
    fun getEventsDataFlow(city: String): Flow<List<TicketmasterEvent>>
    fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?>
    suspend fun getEventById(eventId: String): TicketmasterEvent?
}

class EventsRepositoryImpl(private val api: TicketmasterApiService) : EventsRepository {

    override fun getEventsDataFlow(city: String): Flow<List<TicketmasterEvent>> {
        return flow {
            emit(api.loadEvents(city = city))
        }
    }

    override fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?> {
        return flow {
            emit(api.getEventById(eventId))
        }
    }

    override suspend fun getEventById(eventId: String): TicketmasterEvent? {
        return api.getEventById(eventId)
    }
}





