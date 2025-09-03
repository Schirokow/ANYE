package org.example.anye.viewmodels

import org.example.anye.data.Favorite
import org.example.anye.data.FavoriteRepository
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.anye.logMessage

class FavoriteViewModel(private val favoriteRepository: FavoriteRepository) : ViewModel() {
    private val _favoriteEvents = MutableStateFlow<List<Favorite>>(viewModelScope,emptyList())
    val favoriteEvents: StateFlow<List<Favorite>> = _favoriteEvents.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.getFavoriteEvents().collect { favorites ->
                _favoriteEvents.value = favorites
            }
        }
    }

    fun toggleFavorite(favoriteEvent: Favorite) {
        viewModelScope.launch {
            try {
                if (favoriteRepository.isFavorite(favoriteEvent.eventId)) {
                    favoriteRepository.removeFavorite(favoriteEvent.eventId)
                    logMessage("FavoriteViewModel: Removed favorite: ${favoriteEvent.eventId}")
                }
            } catch (e: Exception) {
                logMessage("FavoriteViewModel: Error toggling favorite: ${e.message}")
            }
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            try {
                favoriteRepository.deleteAllFavorites()
                _favoriteEvents.value = emptyList() // UI sofort aktualisieren
                logMessage("FavoriteViewModel: All favorites deleted")
            } catch (e: Exception) {
                logMessage("FavoriteViewModel: Error deleting all favorites: ${e.message}")
            }
        }
    }
}