package org.example.anye.data

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Event

class GetFirebaseEventsUseCase(private val repository: FirebaseEventRepository) {
    suspend fun getFirebaseEventsFlow(): Flow<List<FirebaseEvent>>{
        return repository.getFirebaseEvents()
    }

    suspend fun addEvent(event: Event){
        repository.addEvent(event)
    }

    suspend fun deleteEvent(eventId: String){
        repository.deleteEvent(eventId)
    }

    suspend fun deleteEventFromFirestore(eventId: String) {
        repository.deleteEventFromFirestore(eventId)
    }

    suspend fun deleteEventCompletely(eventId: String) {
        repository.deleteEventCompletely(eventId)
    }

    suspend fun deleteAllEvents(){
        repository.deleteAllEvents()
    }
}