package org.example.anye.data

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rickclephas.kmp.observableviewmodel.ViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.anye.data.Event
import org.example.anye.logMessage

class EventViewModel(
    private val getFirebaseEventsUseCase: GetFirebaseEventsUseCase
): ViewModel() {

    private val _firebaseEventsData = MutableStateFlow<List<FirebaseEvent>>(emptyList())
    val firebaseEventsData: StateFlow<List<FirebaseEvent>> = _firebaseEventsData.asStateFlow()

    init {
        viewModelScope.launch {
            getFirebaseEventsUseCase.getFirebaseEventsFlow().collect { events ->
                _firebaseEventsData.value = events
            }
        }
    }

    fun addEvent(event: Event){
        viewModelScope.launch {
            try {
                // Prüfe ob ID existiert, bevor du das Event hinzufügst
                if (event.id.isNullOrEmpty()) {
                    logMessage("EventViewModel: Cannot add event with null or empty ID")
                    return@launch
                }
                getFirebaseEventsUseCase.addEvent(event)
                logMessage("EventViewModel: Event added")
            } catch (e: Exception) {
                logMessage("EventViewModel: Error adding event: ${e.message}")
            }
        }

    }

//    fun loadFirebaseEvents(){
//        viewModelScope.launch {
//            try {
//
//                getFirebaseEventsUseCase.getFirebaseEventsFlow().collect { evetnts ->
//                    _firebaseEventsData.value = evetnts
//                }
//                logMessage("EventViewModel: Events loaded")
//
//            }catch (e: Exception) {
//                logMessage("EventViewModel: Error loading events: ${e.message}")
//            }
//
//        }
//    }

    fun deleteEvent(eventId: String){
        viewModelScope.launch {
            try {
                getFirebaseEventsUseCase.deleteEvent(eventId)
                logMessage("EventViewModel: Event delead")
            } catch (e: Exception) {
                logMessage("EventViewModel: Error deleading event: ${e.message}")
            }
        }
    }

    fun deleteEventFromFirestore(eventId: String) {
        viewModelScope.launch {
            try {
                getFirebaseEventsUseCase.deleteEventFromFirestore(eventId)
                logMessage("EventViewModel: Event deleted from Firestore")
            } catch (e: Exception) {
                logMessage("EventViewModel: Error deleting event from Firestore: ${e.message}")
            }
        }
    }

    fun deleteEventCompletely(eventId: String) {
        viewModelScope.launch {
            try {
                getFirebaseEventsUseCase.deleteEventCompletely(eventId)
                logMessage("EventViewModel: Event completely deleted from both sources")
            } catch (e: Exception) {
                logMessage("EventViewModel: Error completely deleting event: ${e.message}")
            }
        }
    }


    fun saveEvent(
        fs: FirebaseFirestore,
        title: String,
        description: String,
        city: String,
        start: String,
        userId: String,
        snackbarHostState: SnackbarHostState? = null,
        scope: CoroutineScope? = null
    ){

        // Erstelle eine neue Dokument-Referenz mit automatisch generierter ID
        val newDocRef = fs.collection("events").document()
        val documentId = newDocRef.id

        // Stelle sicher, dass documentId nicht leer ist
        if (documentId.isEmpty()) {
            Log.e("CreateEventScreen", "Generated document ID is empty")
            scope?.launch {
                snackbarHostState?.showSnackbar("Fehler beim Erstellen des Events: Keine ID generiert")
            }
            return
        }

        // Erstelle das Event-Objekt MIT der ID
        val event = Event(
            id = documentId, // WICHTIG: ID hier setzen
            userId = userId,
            imageUrl = null,
            title = title,
            description = description,
            city = city,
            startData = start,
        )

        // Speichert den Event in FirebaseEventDatabase
        addEvent(event)

        // Speichere das Event mit der spezifischen Dokument-ID
        newDocRef.set(event)
            .addOnSuccessListener {
                Log.d("CreateEventScreen", "Event erfolgreich erstellt mit ID: $documentId")
                // Feedback an Benutzer geben
                scope?.launch {
                    snackbarHostState?.showSnackbar("Event erfolgreich erstellt!")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventScreen", "Fehler beim Erstellen des Events: ${e.message}")
                scope?.launch {
                    snackbarHostState?.showSnackbar("Fehler beim Erstellen des Events")
                }
            }


//    fs.collection("events")
//        .document().set(
//            Event(
//                userId = "2",
//                imageUrl = "TestUrl",
//                title = title,
//                description = description,
//                city = city,
//                startData = start,
//                location = Location("100", "200")
//            )
//        )
    }


}