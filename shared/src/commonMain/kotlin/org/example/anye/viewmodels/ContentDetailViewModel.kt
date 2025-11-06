package org.example.anye.viewmodels

import org.example.anye.data.FavoriteRepository
import org.example.anye.data.TicketmasterEvent
import org.example.anye.usecases.GetEventByIdUseCase
import org.example.anye.usecases.GetEventsUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import org.example.anye.logMessage
import org.example.anye.usecases.GetFavoriteUseCase

class ContentDetailViewModel(
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val eventsUseCase: GetEventsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase
) : ViewModel() {

    private val _action = MutableSharedFlow<ContentDetailAction>()
    val action = _action.asSharedFlow()


    private val _event = MutableStateFlow<TicketmasterEvent?>(viewModelScope,null)
    val event: StateFlow<TicketmasterEvent?> = _event.asStateFlow()

    private val _isFavorite = MutableStateFlow(viewModelScope,false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()


    fun loadEvent(id: String) {
        viewModelScope.launch {
            try {
                // Lade das Event direkt über die API-ID
                val eventData = getEventByIdUseCase.getEventByIdFlow(id).firstOrNull()
                _event.value = eventData
                _action.emit(ContentDetailAction.Success("Detailansicht von ${eventData?.name.toString()}"))
            } catch (e: Exception) {
                logMessage("ContentDetailViewModel: Error loading event: ${e.message}")
                _event.value = null
                _action.emit(ContentDetailAction.Success("Fehler beim laden von Event!"))
            }
        }
    }

    fun toggleFavorite(event: TicketmasterEvent) {
        viewModelScope.launch {
            try {
                if (getFavoriteUseCase.isFavorite(event.id)) {
                    getFavoriteUseCase.removeFavorite(event.id)
                    _isFavorite.value = false
                    _action.emit(ContentDetailAction.Success("Von Favoriten entfernt"))
                    logMessage("ContentDetailViewModel: Removed favorite: ${event.id}")
                } else {
                    getFavoriteUseCase.addFavorite(event)
                    _isFavorite.value = true
                    _action.emit(ContentDetailAction.Success("Zu Favoriten hinzugefügt"))
                    logMessage("ContentDetailViewModel: Added favorite: ${event.id}")
                }
            } catch (e: Exception) {
                logMessage("ContentDetailViewModel: Error toggling favorite: ${e.message}")
                _action.emit(ContentDetailAction.Success("Fehler!"))
            }
        }
    }
}


sealed class ContentDetailAction {
    data class Success(val message: String) : ContentDetailAction()
    data class Error(val message: String) : ContentDetailAction()
    object Initial : ContentDetailAction() // Ein neutraler Startzustand
}