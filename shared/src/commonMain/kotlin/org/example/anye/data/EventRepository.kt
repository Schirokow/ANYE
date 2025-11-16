package org.example.anye.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.anye.data.ticketmaster_data_classes.TicketmasterEvent

interface EventsRepository {
    fun getEventsDataFlow(city: String): Flow<List<TicketmasterEvent>>
    fun getEventByIdFlow(eventId: String): Flow<TicketmasterEvent?>

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

}





