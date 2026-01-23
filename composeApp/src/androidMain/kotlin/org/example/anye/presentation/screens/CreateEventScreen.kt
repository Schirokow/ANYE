package org.example.anye.presentation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.data.Event
import org.example.anye.data.Location
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.viewmodels.AuthResult
import org.example.anye.viewmodels.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateEventScreen(navController: NavController) {

    val viewModel: LoginViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val fs = Firebase.firestore

    var titleState by remember { mutableStateOf("") }
    var descriptionState by remember { mutableStateOf("") }
    var locationState by remember { mutableStateOf("") }
    var dataState by remember { mutableStateOf("") }

    // Zustand für den Bestätigungsdialog
    var showDeleteDialog by remember { mutableStateOf(false) }


    // Auf Events vom ViewModel hören
    LaunchedEffect(Unit) {
        viewModel.authResult.collect { result ->
            when (result) {
                is AuthResult.Success -> {
                    snackbarHostState.showSnackbar(message = result.message)
                    // Navigation nach erfolgreichem Löschen
                    navController.navigate("LoginScreen") {
                        popUpTo("AccountScreen") {
                            inclusive = true
                        } // verhindert zurück zur Login-Seite
                    }
                }

                is AuthResult.Error -> {
                    snackbarHostState.showSnackbar(message = result.message)
                }

                is AuthResult.Initial -> {
                    // Nichts tun beim Start
                }
            }
        }
    }


    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Account erfolgreich gelöscht" -> Color(0xFF4CAF50) // Grün
                    "Passwörter stimmen nicht überein" -> Color(0xFFF44336) // Rot
                    else -> Color(0xFF2196F3) // Blau (Standard)
                }
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    containerColor = background,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = AccentColor,
    ) { paddingValues -> // paddingValues berücksichtigt die Systemleisten

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TopLightBlue,
                            BottomDarkBlue
                        )
                    )
                )
        ) {
            // Auth-Status oben rechts
            AuthStatusIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "ArrowBack",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
                        .size(34.dp)
                        .clickable {
                            Log.d("AccountScreen", "Navigating to ProfileScreen")
                            navController.navigate("ProfileScreen")
                        }

                )
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clickable {
                            Log.d("AccountScreen", "Navigating to ProfileScreen")
                            navController.navigate("ProfileScreen")
                        }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                Image(
                    painter = painterResource(id = org.example.anye.R.drawable.logo_anye),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .padding(top = 100.dp)
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    "Event erstellen",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(30.dp))


                OutlinedTextField(
                    value = titleState,
                    onValueChange = {
                        titleState = it
                    },
                    placeholder = { Text("Titel", color = Color.White) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    value = descriptionState,
                    onValueChange = {
                        descriptionState = it
                    },
                    placeholder = { Text("Beschreibung", color = Color.White) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    value = locationState,
                    onValueChange = { locationState = it },
                    placeholder = { Text("Ort", color = Color.White) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                OutlinedTextField(
                    value = dataState,
                    onValueChange = { dataState = it },
                    placeholder = { Text("Startdatum", color = Color.White) },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Yellow,
                        unfocusedBorderColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(30.dp))


                Spacer(modifier = Modifier.height(8.dp))

                ClickButton(
                    text = "Event Erstellen",
                    onClick = {
                        if (titleState.isNotBlank() && descriptionState.isNotBlank() && locationState.isNotBlank() && dataState.isNotBlank()) {
                            showDeleteDialog = true
                        }  else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Bitte alle Felder ausfühlen!")
                            }
                        }

                    },
                    modifier = Modifier
                        .padding(horizontal = 120.dp)
                        .fillMaxWidth()
                )

                // AlerDer Bestätigungsdialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                "Event erstellen?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                "Bist du sicher, dass du neuen Event erstellen willst?",
                                color = Color.White
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    saveEvent(
                                        fs = fs,
                                        title = titleState,
                                        description = descriptionState,
                                        start = dataState,
                                        city = locationState,
                                        snackbarHostState = snackbarHostState,
                                        scope = scope
                                    )
                                    titleState = ""
                                    descriptionState = ""
                                    dataState = ""
                                    locationState = ""
                                    showDeleteDialog = false


                                },
                                colors = buttonColors(
                                    containerColor = Color.Green
                                )
                            ) {
                                Text("Ja, erstellen", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = buttonColors(
                                    containerColor = Color.Blue
                                )
                            ) {
                                Text("Abbrechen", color = Color.White)
                            }
                        },
                        containerColor = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

private fun saveEvent(
    fs: FirebaseFirestore,
    title: String,
    description: String,
    city: String,
    start: String,
    snackbarHostState: SnackbarHostState? = null,
    scope: kotlinx.coroutines.CoroutineScope? = null
){

    // Erstelle eine neue Dokument-Referenz mit automatisch generierter ID
    val newDocRef = fs.collection("events").document()
    val documentId = newDocRef.id

    // Erstelle das Event-Objekt MIT der ID
    val event = Event(
        id = documentId, // WICHTIG: ID hier setzen
        userId = "2",
        imageUrl = "TestUrl",
        title = title,
        description = description,
        city = city,
        startData = start,
        location = Location("100", "200")
    )

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
