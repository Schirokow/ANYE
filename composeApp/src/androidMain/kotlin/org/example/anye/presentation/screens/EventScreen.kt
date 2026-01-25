package org.example.anye.presentation.screens

import android.app.Application
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.data.EventViewModel
import org.example.anye.data.EventViewModelFactory
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.ui.components.card.EventCard
import org.example.anye.ui.components.card.FirebaseEventCard
import org.example.anye.viewmodels.AuthResult
import org.example.anye.viewmodels.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EventScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(context.applicationContext as Application)
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userEvent = ""
    val itemsPerRow = 2
    val firebaseEvents by viewModel.firebaseEventsData.collectAsState()


    // Zustand für den Bestätigungsdialog
    var showDeleteDialog by remember { mutableStateOf(false) }



    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Event erfolgreich gelöscht!"-> Color(0xFF4CAF50) // Grün
                    "Fehler beim Löschen des Events" -> Color(0xFFF44336) // Rot
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
                    "Eigene Events",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                if (firebaseEvents.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Keine eigene Events", color = Color.White, fontSize = 20.sp)
                    }
                } else {
//                    FavoriteContent(navController, viewModel)


                    Spacer(modifier = Modifier.height(30.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(itemsPerRow),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)

                    ) {
                        items(firebaseEvents.size) { index ->
                            val event = firebaseEvents[index]
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .aspectRatio(1f)
                            ) {
                                FirebaseEventCard(
                                    event = event,
                                    modifier = Modifier,
                                    onClick = {
//                                    selectedFirestoreEvent = event
                                    },
                                    isLarge = true,
                                    textIsLarge = false
                                )
                            }

                        }
                    }
                }

                // AlerDer Bestätigungsdialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                "Event löschen?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                "Bist du sicher, dass du dieses Event löschen willst?",
                                color = Color.White
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {


                                },
                                colors = buttonColors(
                                    containerColor = Color.Green
                                )
                            ) {
                                Text("Ja, löschen", color = Color.White)
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