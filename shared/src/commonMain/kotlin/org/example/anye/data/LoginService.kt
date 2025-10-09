package org.example.anye.data



data class AuthUser(val uid: String, val email: String?)

interface LoginService {
    fun signUp(email: String, password: String, username: String, onResult: (Boolean) -> Unit)
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit)
    fun deleteAccount(email: String, password: String, onResult: (Boolean) -> Unit)
    fun signOut()
    fun getCurrentUser(): AuthUser?
//    val currentUser: AuthUser?
}