package org.example.anye.viewmodels

import org.example.anye.data.FavoriteRepository
import org.example.anye.data.TicketmasterEvent
import org.example.anye.usecases.GetEventsUseCase
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.anye.logMessage
import org.example.anye.usecases.GetFavoriteUseCase

class HomeViewModel(
    private val getFavoriteUseCase: GetFavoriteUseCase,
    private val getEventsUseCase: GetEventsUseCase,
) : ViewModel() {


    private val _isLoading = MutableStateFlow(viewModelScope,false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _eventsData = MutableStateFlow<List<TicketmasterEvent>>(viewModelScope,emptyList())
    val eventsData: StateFlow<List<TicketmasterEvent>> = _eventsData.asStateFlow()

    suspend fun isFavorite(eventId: String): Boolean {
        return getFavoriteUseCase.isFavorite(eventId)
    }


    fun loadAllEvents(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getEventsUseCase.getEventsFlow(city).collect { evetnts ->
                _eventsData.value = evetnts
            }
            _isLoading.value = false
        }
    }

    fun deleteAllEvents() {
        viewModelScope.launch {
            try {
//                festivalRepository.deleteAllFestivals()
                _eventsData.value = emptyList()
                logMessage("HomeViewModel: All festivals deleted")
            } catch (e: Exception) {
                logMessage("HomeViewModel: Error deleting festivals: ${e.message}")
            }
        }
    }

    fun toggleFavorite(event: TicketmasterEvent) {
        viewModelScope.launch {
            try {
                if (getFavoriteUseCase.isFavorite(event.id)) {
                    getFavoriteUseCase.removeFavorite(event.id)
                    logMessage("HomeViewModel: Removed favorite: ${event.id}")
                } else {
                    getFavoriteUseCase.addFavorite(event)
                    logMessage("HomeViewModel: Added favorite: ${event.id}")
                }
            } catch (e: Exception) {
                logMessage("HomeViewModel: Error toggling favorite: ${e.message}")
            }
        }
    }
}