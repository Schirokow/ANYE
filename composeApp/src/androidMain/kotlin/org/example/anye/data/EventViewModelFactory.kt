package org.example.anye.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore

class EventViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            // Hier erstellst du die Abhängigkeiten manuell
            val database = FirebaseEventDatabase.getDatabase(application)
            val dao = database.firebaseEventDao()
            val firestore = FirebaseFirestore.getInstance()
            val repository = FirebaseEventRepositoryImpl(dao)
            val useCase = GetFirebaseEventsUseCase(repository)

            @Suppress("UNCHECKED_CAST")
            return EventViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}