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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import org.example.anye.logMessage

class ContentDetailViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val eventsUseCase: GetEventsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase
) : ViewModel() {



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
            } catch (e: Exception) {
                logMessage("ContentDetailViewModel: Error loading event: ${e.message}")
                _event.value = null
            }
        }
    }

    fun toggleFavorite(event: TicketmasterEvent) {
        viewModelScope.launch {
            try {
                if (favoriteRepository.isFavorite(event.id)) {
                    favoriteRepository.removeFavorite(event.id)
                    _isFavorite.value = false
                    logMessage("ContentDetailViewModel: Removed favorite: ${event.id}")
                } else {
                    favoriteRepository.addFavorite(event)
                    _isFavorite.value = true
                    logMessage("ContentDetailViewModel: Added favorite: ${event.id}")
                }
            } catch (e: Exception) {
                logMessage("ContentDetailViewModel: Error toggling favorite: ${e.message}")
            }
        }
    }
}