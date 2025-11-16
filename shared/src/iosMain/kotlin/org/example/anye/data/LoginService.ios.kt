package org.example.anye.data

class LoginServiceiOS : LoginService {
//    override val currentUser: AuthUser? = null

    override fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        // TODO: Implement with Firebase iOS SDK (via Cocoapods/SPM)
        onResult(false)
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
