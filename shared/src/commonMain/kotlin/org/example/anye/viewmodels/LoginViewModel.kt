package org.example.anye.viewmodels

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.stateIn
import com.rickclephas.kmp.observableviewmodel.MutableStateFlow
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.anye.data.AuthUser
import org.example.anye.data.User
import org.example.anye.logMessage
import org.example.anye.usecases.GetLoginServiceUseCase
import org.example.anye.usecases.GetUsersUseCase

class LoginViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val getLoginServiceUseCase: GetLoginServiceUseCase
) : ViewModel() {


    // StateFlow für Users
    private val _users = MutableStateFlow<List<User>>(viewModelScope, emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // SharedFlow für Auth-Events
    private val _authResult = MutableSharedFlow<AuthResult>()
    val authResult = _authResult.asSharedFlow()

    // Kontinuierlicher Flow
    init {
        viewModelScope.launch {
            getUsersUseCase.getUsersFlow().collect { users ->
                _users.value = users
            }
        }
    }

    fun addUser(user: User) {
        getUsersUseCase.addUser(user)
    }

    fun signUp(email: String, password: String) {
        getLoginServiceUseCase.signUp(email, password)
    }

    //    fun signIn(email: String, password: String){
//        getLoginServiceUseCase.signIn(email,password)
//    }
    fun signIn(email: String, password: String) {
        // Wir übergeben eine Lambda-Funktion an den Service,
        // die aufgerufen wird, wenn das Ergebnis von Firebase da ist.
        getLoginServiceUseCase.signIn(email, password) { isSuccess ->
            viewModelScope.launch {
                if (isSuccess) {
                    _authResult.emit(AuthResult.Success("Anmeldung erfolgreich!"))
                } else {
                    _authResult.emit(AuthResult.Error("E-Mail oder Passwort ist falsch"))
                }
            }
        }
    }

    fun signOut() {
        getLoginServiceUseCase.signOut()
        viewModelScope.launch {
            _authResult.emit(AuthResult.Success("Erfolgreich abgemeldet"))
        }
    }

//    fun signOut() {
//        getLoginServiceUseCase.signOut()
//    }

    fun deleteAccount(email: String, password: String) {
        getLoginServiceUseCase.deleteAccount(email, password)
    }

    fun getCurrentUser(): AuthUser? {
        return getLoginServiceUseCase.getCurrentUser()
    }

}

// Repräsentiert die möglichen Ergebnisse einer Authentifizierungs-Aktion
sealed class AuthResult {
    data class Success(val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Initial : AuthResult() // Ein neutraler Startzustand
}