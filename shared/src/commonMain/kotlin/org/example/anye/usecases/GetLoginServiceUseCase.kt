package org.example.anye.usecases

import org.example.anye.data.AuthUser
import org.example.anye.data.LoginService

class GetLoginServiceUseCase(private val auth: LoginService) {

    fun signUp(email: String, password: String) {
        auth.signUp(email, password){ success ->
            if (success) {
                println("Registration success")
            } else {
                println("Registration failed")
            }
        }

    }

    fun signIn(email: String, password: String) {
        auth.signIn(email, password) { success ->
            if (success) {
                println("User signed in")
            } else {
                println("Sign in failed")
            }
        }
    }

    fun signOut(){
        auth.signOut()
    }

    fun deleteAccount(email: String, password: String) {
        auth.deleteAccount(email,password){ success ->
            if (success) {
                println("Account deleted")
            } else {
                println("Delete Account failed")
            }

        }

    }

    fun getCurrentUser(): AuthUser?{
        return auth.getCurrentUser()
    }

}