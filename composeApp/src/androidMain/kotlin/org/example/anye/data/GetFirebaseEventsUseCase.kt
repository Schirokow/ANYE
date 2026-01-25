package org.example.anye.data

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Event

class GetFirebaseEventsUseCase(private val events: FirebaseEventRepository) {
    suspend fun getFirebaseEventsFlow(): Flow<List<FirebaseEvent>>{
        return events.getFirebaseEvents()
    }

    suspend fun addEvent(event: Event){
        events.addEvent(event)
    }

    suspend fun deleteEvent(eventId: String){
        events.deleteEvent(eventId)
    }

    suspend fun deleteAllEvents(){
        events.deleteAllEvents()
    }
}