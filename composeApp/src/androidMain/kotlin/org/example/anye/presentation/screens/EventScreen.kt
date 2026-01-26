package org.example.anye.presentation.screens

import android.app.Application
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.data.EventViewModel
import org.example.anye.data.EventViewModelFactory
import org.example.anye.data.FirebaseEvent
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.components.card.FirebaseEventCard

@Composable
fun EventScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(context.applicationContext as Application)
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val itemsPerRow = 2
    val firebaseEvents by viewModel.firebaseEventsData.collectAsState()

    // State für ausgewählte Events
    var selectedEvent by remember { mutableStateOf<FirebaseEvent?>(null) }


    // State für den Bestätigungsdialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<String?>(null) }

    // Animation für die Vergrößerung
    val animateScale by animateFloatAsState(
        targetValue = if (selectedEvent != null) 1f else 0.5f,
        animationSpec = tween(durationMillis = 400)
    )


    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Event gelöscht!" -> Color(0xFF4CAF50)
                    "Fehler beim Löschen des Events" -> Color(0xFFF44336)
                    else -> Color(0xFF2196F3)
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
                            Log.d("EventScreen", "Navigating to ProfileScreen")
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
                                        selectedEvent = event
                                    },
                                    isLarge = true,
                                    textIsLarge = false
                                )
                            }

                        }
                    }
                }

            }

            // Vergrößerte Ansicht (Overlay)
            selectedEvent?.let { event ->
                Surface(
                    color = BackgroundColor.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        // Klick außerhalb schließt das Overlay
//                        selectedEvent = null
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FirebaseEventCard(
                                event = event,
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = animateScale,
                                        scaleY = animateScale
                                    )
                                    .fillMaxWidth(0.9f)
                                    .height(300.dp),
                                onClick = {},
                                isLarge = true,
                                textIsLarge = true
                            )
                        }

                        // Schließen-Button
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(24.dp)
                                .size(34.dp)
                                .clickable {
                                    Log.d("EventScreen", "Close button clicked")
                                    selectedEvent = null
                                }
                        )

                        // Löschen-Button
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                                .size(34.dp)
                                .clickable {
                                    Log.d("EventScreen", "Delete button clicked for event: ${event.id}")
                                    eventToDelete = event.id
                                    showDeleteDialog = true
                                }
                        )
                    }
                }
            }

            // Bestätigungsdialog für Löschen
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        eventToDelete = null
                    },
                    title = {
                        Text(
                            "Event löschen?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "Möchtest du dieses Event wirklich löschen?",
                            color = Color.White
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                eventToDelete?.let { eventId ->
                                    viewModel.deleteEventCompletely(eventId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Event gelöscht!")
                                    }
                                    selectedEvent = null
                                }
                                showDeleteDialog = false
                                eventToDelete = null
                            },
                            colors = buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("Löschen", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                eventToDelete = null
                            },
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

