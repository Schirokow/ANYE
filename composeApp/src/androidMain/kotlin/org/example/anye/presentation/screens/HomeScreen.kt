package org.example.anye.presentation.screens

// Für Bildanzeige

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.data.TicketmasterEvent
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.ui.components.card.NewEventCard
import org.example.anye.ui.menu.AnyeBottomBar
import org.example.anye.viewmodels.Action
import org.example.anye.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel


// Startseite
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = koinViewModel()) {
    val TAG = "HomeScreen"
    Log.d(TAG, "Home screen initialized")

//    val viewModel: HomeViewModel = koinViewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val eventsData by viewModel.eventsData.collectAsState()
    var city by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        viewModel.action.collect { action ->
            when (action) {
                is Action.Success -> {
                    snackbarHostState.showSnackbar(action.message)
                }

                is Action.Error -> {
                    snackbarHostState.showSnackbar(action.message)
                }

                Action.Initial -> Unit
            }
        }
    }

    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Events erfolgreich geladen" -> Color(0xFF4CAF50) // Grün
                    "Zu Favoriten hinzugefügt" -> Color(0xFF4CAF50) // Grün
                    "Fehler beim Laden der Events" -> Color(0xFFF44336) // Rot
                    "Fehler beim Löschen!" -> Color(0xFFF44336) // Rot
                    "Fehler beim Favorisieren!" -> Color(0xFFF44336) // Rot
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
    ) { paddingValues ->

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = city,
                        singleLine = true,
                        placeholder = { Text("Stadt eingeben", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Yellow,
                            unfocusedBorderColor = Color.White
                        ),
                        onValueChange = { city = it },
                        modifier = Modifier.padding(start = 65.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Add",
                        tint = Color.Blue,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                if (city.isNotBlank()) {
//                            viewModel.loadAllFestivals()
                                    viewModel.loadAllEvents(city)
                                    city = ""
                                }
                            }
                    )
                }

                AnimatedVisibility(
                    visible = eventsData.isNotEmpty() && !isLoading
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClickButton(
                            text = "Auf der Karte",
                            onClick = {
//                            if (city.isNotBlank()) {
////                            viewModel.loadAllFestivals()
//                                viewModel.loadAllEvents(city)
//                                city = ""
//                            }

                            },
                            modifier = Modifier.width(150.dp)
                        )

                        ClickButton(
                            text = "Löschen",
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.width(150.dp)
                        )

                    }
                }

            }
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "Lade...",
                            color = Color.White,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(top = 70.dp)
                        )
                    }
                }

                else -> {
                    // Funktion für die Vorschau.
                    EventContent(navController)
                }
            }


            AnyeBottomBar(navController)
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Alle Events löschen?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Möchtest du wirklich alle Events aus der Liste löschen? Favoriten bleiben erhalten.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAllEvents()
                        Log.d(TAG, "All events deleted")
                    },
                    colors = buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Ja, löschen", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = buttonColors(
                        containerColor = Color.Green
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


@Composable
fun EventContent(navController: NavController, viewModel: HomeViewModel = koinViewModel()) {

    val TAG = "EventContent"
//    val viewModel: HomeViewModel = koinViewModel()
    val eventsDataList by viewModel.eventsData.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val favoriteStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(eventsDataList) {
        eventsDataList.forEach { event ->
            coroutineScope.launch {
                val isFavorite = viewModel.isFavorite(event.id)
                favoriteStates[event.id] = isFavorite
            }
        }
    }

    // Schutz vor leeren Listen
    if (
        eventsDataList.isEmpty()
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Keine Events geladen", color = Color.White, fontSize = 20.sp)
        }
        return
    }

    // State, um ausgewählte EventData zu speichern
    var selectedEventData by remember {
        mutableStateOf<TicketmasterEvent?>(null).also {
            Log.d(TAG, "Selected festival state initialized")
        }
    }

    Log.d(TAG, "Rendering event grid with ${eventsDataList.size} items")


    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 155.dp),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 500.dp
        ),
        columns = GridCells.Fixed(2)
    ) {
        itemsIndexed(eventsDataList) { index, event ->

            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .aspectRatio(1f)
            ) {
                NewEventCard(
                    image = event.images,
                    title = event.name,
                    datum = event.dates?.start?.localDate,
                    onClick = {
                        Log.d(
                            TAG,
                            "Festival card clicked - id: ${event.id}, title: ${event.name.take(15)}..."
                        )
                        selectedEventData = event
                    },
                    isLarge = true,
                    modifier = Modifier
                )
            }
        }
    }


    val animateScale by animateFloatAsState(
        targetValue = if (selectedEventData != null) 1f else 0.5f,
        animationSpec = tween(durationMillis = 400)
    )

    // Overlay für vergrößertes Bild, wenn selectedEventData nicht null ist
    selectedEventData?.let { event ->
        Log.d(TAG, "Showing detail overlay for id: ${event.id}")

        Surface(
            color = BackgroundColor.copy(alpha = 0.9f), // Farbe von Hintergrund
            modifier = Modifier.fillMaxSize(),
            onClick = { /*selectedEventData = null */ } // Klick außerhalb schließt das Overlay
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
//                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NewEventCard(
                        image = event.images,
                        title = event.name,
                        datum = event.dates?.start?.localDate,
                        onClick = {
                            Log.d(TAG, "Navigating to detail screen for id: ${event.id}")
                            navController.navigate("ContentDetailScreen/${event.id}")
                        },
                        isLarge = true,
                        textIsLarge = true,
                        modifier = Modifier
                            .graphicsLayer(scaleX = animateScale, scaleY = animateScale)
                            .fillMaxWidth(0.9f)
//                            .fillMaxHeight(0.5f)
                            .height(300.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .padding(24.dp)
                        .size(34.dp)
                        .clickable {
                            Log.d(TAG, "Close button clicked, hiding detail view")
                            selectedEventData = null
                        }
                )

                Icon(
                    imageVector = if (favoriteStates[event.id] == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (favoriteStates[event.id] == true) Color.Yellow else Color.White,
                    modifier = Modifier
                        .align(alignment = Alignment.TopEnd)
                        .padding(24.dp)
                        .size(34.dp)
                        .clickable {
                            Log.i(TAG, "Favorite button clicked for id: ${event.id}")
                            viewModel.toggleFavorite(event)
                            favoriteStates[event.id] = !(favoriteStates[event.id] ?: false)
                        }
                )
                //Erklärung:
                //Der Favoritenstatus wird mit favoriteStates (eine mutableStateMapOf) dynamisch geladen, um UI-Reaktivität zu gewährleisten.
                //LaunchedEffect lädt den Favoritenstatus für jedes Festival beim Rendern.
                //Der Favoriten-Button toggelt den Status und aktualisiert favoriteStates.
            }

        }
    }

}

val BackgroundColor = Color(0xFF20587B)


