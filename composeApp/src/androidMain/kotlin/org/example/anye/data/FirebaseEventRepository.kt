package org.example.anye.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
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
    suspend fun deleteEventFromFirestore(eventId: String) // Löscht aus Firestore
    suspend fun deleteEventCompletely(eventId: String) // Löscht aus beiden
    suspend fun isFavorite(eventId: String): Boolean
    suspend fun getFirebaseEvents(): Flow<List<FirebaseEvent>>
    suspend fun deleteAllEvents()
}

class FirebaseEventRepositoryImpl(
    private val eventDao: FirebaseEventDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FirebaseEventRepository {

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

    override suspend fun deleteEventFromFirestore(eventId: String) {
        try {
            firestore.collection("events").document(eventId).delete().await()
        } catch (e: Exception) {
            println("Error deleting from Firestore: ${e.message}")
        }
    }

    override suspend fun deleteEventCompletely(eventId: String) {
        // Zuerst aus Room löschen
        eventDao.deleteEvent(eventId)
        // Dann aus Firestore löschen
        deleteEventFromFirestore(eventId)
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
