package org.example.anye.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import org.example.anye.data.dao.FavoriteDao

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val eventId: String, // Verweist auf die ID eines Events
    val name: String,
    val imageUrl: String, // Speichere die URL des Bildes
    val date: String,
    val venueName: String,
    val url: String // Speichere die URL des Events
)

interface FavoriteRepository {
    suspend fun addFavorite(event: TicketmasterEvent)
    suspend fun removeFavorite(eventId: String)
    fun getFavoriteEvents(): Flow<List<Favorite>>
    suspend fun isFavorite(eventId: String): Boolean
    suspend fun deleteAllFavorites()
}

class FavoriteRepositoryImpl(private val favoriteDao: FavoriteDao) : FavoriteRepository {

    // Die Funktion konvertiert jetzt ein TicketmasterEvent in eine Favorite-Entität
    override suspend fun addFavorite(event: TicketmasterEvent) {
        val favorite = Favorite(
            eventId = event.id,
            name = event.name,
            imageUrl = event.images?.firstOrNull()?.url ?: "", // Sichere Methode, die erste URL zu holen
            date = event.dates?.start?.localDate ?: "N/A",
            venueName = event._embedded?.venues?.firstOrNull()?.name ?: "Unbekannter Ort",
            url = event.url
        )
        favoriteDao.insertFavorite(favorite)
    }

    override suspend fun removeFavorite(eventId: String) {
        favoriteDao.deleteFavorite(eventId)
    }

    override fun getFavoriteEvents(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites()
    }

    override suspend fun isFavorite(eventId: String): Boolean {
        return favoriteDao.isFavorite(eventId)
    }

    override suspend fun deleteAllFavorites() {
        favoriteDao.deleteAllFavorites()
    }
}
