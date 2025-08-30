package org.example.anye.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.anye.business.usecases.GetUsersUseCase
import org.example.anye.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Profile1ViewModel: ViewModel() {
    private val getUsersUseCase: GetUsersUseCase = GetUsersUseCase()

    // StateFlow für Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Kontinuierlicher Flow
    init {
        viewModelScope.launch {
            getUsersUseCase.getUsersFlow().collect { users ->
                _users.value = users
            }
        }
    }
}