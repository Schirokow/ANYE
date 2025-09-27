package org.example.anye.usecases

import org.example.anye.data.LoginService

class GetLoginServiceUseCase(private val auth: LoginService) {

    fun signIn(email: String, password: String) {
        auth.signIn(email, password) { success ->
            if (success) {
                println("User signed in: ${auth.currentUser}")
            } else {
                println("Sign in failed")
            }
        }
    }

}