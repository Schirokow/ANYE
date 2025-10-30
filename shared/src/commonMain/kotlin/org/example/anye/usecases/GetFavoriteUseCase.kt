package org.example.anye.usecases

import kotlinx.coroutines.flow.Flow
import org.example.anye.data.Favorite
import org.example.anye.data.FavoriteRepository
import org.example.anye.data.TicketmasterEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GetFavoriteUseCase(): KoinComponent {

    private val favorite: FavoriteRepository by inject()
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