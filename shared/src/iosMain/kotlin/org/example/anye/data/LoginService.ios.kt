package org.example.anye.data

class LoginServiceiOS : LoginService {
//    override val currentUser: AuthUser? = null

    override fun signUp(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        onResult(false)
    }

    override fun deleteAccount(email: String, password: String, onResult: (Boolean) -> Unit) {
        onResult(false)
    }

    override fun signOut() {}

    override fun getCurrentUser(): AuthUser? {
        TODO()
    }
}
