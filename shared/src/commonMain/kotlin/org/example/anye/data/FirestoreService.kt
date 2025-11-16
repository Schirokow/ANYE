package org.example.anye.data

interface FirestoreService {
    fun addUser(user: User, onResult: (Boolean) -> Unit)
    fun deleteUser(userId: String, onResult: (Boolean) -> Unit)
}