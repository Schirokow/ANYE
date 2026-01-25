package org.example.anye.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Event

@Entity(tableName = "events")
data class FirebaseEvent(
    @PrimaryKey val id: String, // Verweist auf die ID eines Events
    val userId: String?,
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val startData: String?,
    val city: String?,
)

interface FirebaseEventRepository {
    suspend fun addEvent(event: Event)
    suspend fun deleteEvent(eventId: String)
    suspend fun isFavorite(eventId: String): Boolean
    suspend fun getFirebaseEvents(): Flow<List<FirebaseEvent>>
    suspend fun deleteAllEvents()
}

class FirebaseEventRepositoryImpl(private val eventDao: FirebaseEventDao) : FirebaseEventRepository {

    // Die Funktion konvertiert jetzt ein TicketmasterEvent in eine Favorite-Entität
    override suspend fun addEvent(event: Event) {
        val eventId = event.id ?: throw IllegalArgumentException("Event ID cannot be null")
        val firebaseEvent = FirebaseEvent(
            id = eventId,
            userId = event.userId,
            imageUrl = event.imageUrl,
            title = event.title,
            description = event.description,
            startData = event.startData,
            city = event.city
        )
        eventDao.insertEvent(firebaseEvent)
    }

    override suspend fun deleteEvent(eventId: String) {
        eventDao.deleteEvent(eventId)
    }

    override suspend fun getFirebaseEvents(): Flow<List<FirebaseEvent>> {
        return eventDao.getAllEvents()
    }

    override suspend fun isFavorite(eventId: String): Boolean {
        return eventDao.isFavorite(eventId)
    }

    override suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }
}
