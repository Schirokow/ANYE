package org.example.anye.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginServiceAndroid(
    private val firestoreService: FirestoreService = FirestoreServiceAndroid()
) : LoginService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

//    override val currentUser: AuthUser?
//        get() = auth.currentUser?.let { AuthUser(it.uid, it.email) }

    override fun signUp(
        email: String,
        password: String,
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val newUser = User(
                            id = user.uid,
                            username = username,
                            email = email
                        )

                        firestoreService.addUser(newUser) { firestoreSuccess ->
                            if (firestoreSuccess) {
                                Log.d("Auth", "User $username registered and saved in Firestore")
                            } else {
                                Log.e("Auth", "Firestore save failed")
                            }
                        }
                        onResult(true)
                    } else {
                        Log.d("Auth", "Sign Up failure: ${task.exception?.message}")
                        onResult(false)
                    }
                }
            }
    }

    override fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MyLog", "Sign In successful")
                    onResult(true)
                } else {
                    Log.d("MyLog", "Sign In failure: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }

    override fun deleteAccount(email: String, password: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onResult(false)
        val userId = user.uid
        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Log.d("MyLog", "Firebase Auth account deleted")

                                // Firestore-Dokument löschen:
                                firestoreService.deleteUser(userId) { firestoreDeleted ->
                                    if (firestoreDeleted) {
                                        Log.d("Firestore", "User data deleted successfully")
                                    } else {
                                        Log.e("Firestore", "Failed to delete user data")
                                    }
                                    onResult(firestoreDeleted)
                                }
                            } else {
                                Log.e(
                                    "MyLog",
                                    "Error deleting Firebase account: ${deleteTask.exception?.message}"
                                )
                                onResult(false)
                            }
                        }
                } else {
                    Log.d("MyLog", "Reauthentication failed: ${reauthTask.exception?.message}")
                    onResult(false)
                }
            }
    }

    override fun signOut() {
        auth.signOut()
        Log.d("MyLog", "Signed out")
    }

    override fun getCurrentUser(): AuthUser? {
        val user = auth.currentUser
        return user?.let { AuthUser(it.uid, it.email) }
    }
}

