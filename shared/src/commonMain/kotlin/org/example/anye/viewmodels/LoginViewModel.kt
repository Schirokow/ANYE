package org.example.anye.viewmodels

import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.anye.data.AuthUser
import org.example.anye.usecases.GetLoginServiceUseCase


class LoginViewModel(
    private val getLoginServiceUseCase: GetLoginServiceUseCase
) : ViewModel() {


    // SharedFlow für Auth-Events
    private val _authResult = MutableSharedFlow<AuthResult>()
    val authResult = _authResult.asSharedFlow()


    fun signUp(email: String, password: String, username: String) {
        getLoginServiceUseCase.signUpWithUsername(email, password, username) { isSuccess ->
            viewModelScope.launch {
                if (isSuccess) {
                    _authResult.emit(AuthResult.Success("Registrierung erfolgreich!"))
                } else {
                    _authResult.emit(AuthResult.Error("Fehler bei der Registrierung"))
                }
            }
        }


    }

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

    fun deleteAccount(email: String, password: String) {
        getLoginServiceUseCase.deleteAccount(email, password) {}
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