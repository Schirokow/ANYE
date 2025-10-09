package org.example.anye.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreServiceAndroid : FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    override fun addUser(user: User, onResult: (Boolean) -> Unit) {
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User saved: ${user.username}")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user", e)
                onResult(false)
            }
    }

    override fun deleteUser(userId: String, onResult: (Boolean) -> Unit) {
        firestore.collection("users")
            .document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "User document deleted: $userId")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting user document", e)
                onResult(false)
            }
    }
}