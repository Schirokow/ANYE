package org.example.anye.viewmodels

import org.example.anye.data.Favorite
import org.example.anye.data.FavoriteRepository
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.anye.logMessage
import org.example.anye.usecases.GetFavoriteUseCase

class FavoriteViewModel(private val getFavoriteUseCase: GetFavoriteUseCase) : ViewModel() {

    private val _action = MutableSharedFlow<FavoriteAction>()
    val action = _action.asSharedFlow()

    private val _favoriteEvents = MutableStateFlow<List<Favorite>>(viewModelScope,emptyList())
    val favoriteEvents: StateFlow<List<Favorite>> = _favoriteEvents.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoriteUseCase.getFavoriteEvents().collect { favorites ->
                _favoriteEvents.value = favorites
            }
        }
    }

    fun toggleFavorite(favoriteEvent: Favorite) {
        viewModelScope.launch {
            try {
                if (getFavoriteUseCase.isFavorite(favoriteEvent.eventId)) {
                    getFavoriteUseCase.removeFavorite(favoriteEvent.eventId)
                    _action.emit(FavoriteAction.Success("Von Favoriten entfernt"))
                    logMessage("FavoriteViewModel: Removed favorite: ${favoriteEvent.eventId}")
                }
            } catch (e: Exception) {
                _action.emit(FavoriteAction.Success("Fehler beim entfernen!"))
                logMessage("FavoriteViewModel: Error toggling favorite: ${e.message}")
            }
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {
            try {
                getFavoriteUseCase.deleteAllFavorites()
                _favoriteEvents.value = emptyList() // UI sofort aktualisieren
                _action.emit(FavoriteAction.Success("Alle Favoriten gelöscht"))
                logMessage("FavoriteViewModel: All favorites deleted")
            } catch (e: Exception) {
                _action.emit(FavoriteAction.Success("Fehler beim Favoriten löschen!"))
                logMessage("FavoriteViewModel: Error deleting all favorites: ${e.message}")
            }
        }
    }
}

// Repräsentiert die möglichen Ergebnisse einer Aktion
sealed class FavoriteAction {
    data class Success(val message: String) : FavoriteAction()
    data class Error(val message: String) : FavoriteAction()
    object Initial : FavoriteAction() // Ein neutraler Startzustand
}