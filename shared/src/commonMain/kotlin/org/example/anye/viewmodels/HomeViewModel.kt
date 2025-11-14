package org.example.anye.viewmodels

import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.anye.data.ticketmaster_data_classes.TicketmasterEvent
import org.example.anye.logMessage
import org.example.anye.usecases.GetEventsUseCase
import org.example.anye.usecases.GetFavoriteUseCase

class HomeViewModel(
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val getEventsUseCase: GetEventsUseCase,
) : ViewModel() {

    private val _action = MutableSharedFlow<Action>()
    val action = _action.asSharedFlow()


    private val _isLoading = MutableStateFlow(viewModelScope,false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventsData = MutableStateFlow<List<TicketmasterEvent>>(viewModelScope,emptyList())
    val eventsData: StateFlow<List<TicketmasterEvent>> = _eventsData.asStateFlow()

    suspend fun isFavorite(eventId: String): Boolean {
        return getFavoriteUseCase.isFavorite(eventId)
    }


    fun loadAllEvents(city: String) {

        viewModelScope.launch {
            try {
                _isLoading.value = true
                getEventsUseCase.getEventsFlow(city).collect { evetnts ->
                    _eventsData.value = evetnts
                }
                _isLoading.value = false
                _action.emit(Action.Success("Events erfolgreich geladen"))

            }catch (e: Exception) {
                _isLoading.value = false
                _action.emit(Action.Error("Fehler beim Laden der Events"))
            }

        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch {
            try {
//                festivalRepository.deleteAllFestivals()
                _eventsData.value = emptyList()
                _action.emit(Action.Success("Alle Events gelöscht"))
                logMessage("HomeViewModel: All festivals deleted")
            } catch (e: Exception) {
                _action.emit(Action.Error("Fehler beim Löschen!"))
                logMessage("HomeViewModel: Error deleting festivals: ${e.message}")
            }
        }
    }

    fun toggleFavorite(event: TicketmasterEvent) {
        viewModelScope.launch {
            try {
                if (getFavoriteUseCase.isFavorite(event.id)) {
                    getFavoriteUseCase.removeFavorite(event.id)
                    _action.emit(Action.Success("Von Favoriten entfernt"))
                    logMessage("HomeViewModel: Removed favorite: ${event.id}")
                } else {
                    getFavoriteUseCase.addFavorite(event)
                    _action.emit(Action.Success("Zu Favoriten hinzugefügt"))
                    logMessage("HomeViewModel: Added favorite: ${event.id}")
                }
            } catch (e: Exception) {
                _action.emit(Action.Error("Fehler beim Favorisieren!"))
                logMessage("HomeViewModel: Error toggling favorite: ${e.message}")
            }
        }
    }
}

// Repräsentiert die möglichen Ergebnisse einer Aktion
sealed class Action {
    data class Success(val message: String) : Action()
    data class Error(val message: String) : Action()
    object Initial : Action() // Ein neutraler Startzustand
}