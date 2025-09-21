package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Favorite
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.TicketmasterEvent

class GetFavoriteUseCase(private val favorite: FavoriteRepository) {
    suspend fun addFavorite(event: TicketmasterEvent){
        favorite.addFavorite(event)
    }

    suspend fun removeFavorite(eventId: String) {
        favorite.removeFavorite(eventId)
    }

    fun getFavoriteEvents(): Flow<List<Favorite>> {
        return favorite.getFavoriteEvents()
    }

    suspend fun isFavorite(eventId: String): Boolean {
        return favorite.isFavorite(eventId)
    }

    suspend fun deleteAllFavorites() {
        favorite.deleteAllFavorites()
    }
}