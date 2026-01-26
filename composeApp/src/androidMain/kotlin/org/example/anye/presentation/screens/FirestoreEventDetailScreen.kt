package org.example.anye.presentation.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.R
import org.example.anye.TopLightBlue
import org.example.anye.data.Event
import org.example.anye.data.MapDataHolder
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.ui.menu.AnyeBottomBar
import org.example.anye.viewmodels.ContentDetailAction
import org.example.anye.viewmodels.ContentDetailViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder

@Composable
fun FirestoreEventDetailScreen(navController: NavController, id: String) {
    val TAG = "FirestoreEventDetailScreen"
    Log.d(TAG, "Screen initialized with id: $id")

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val db = Firebase.firestore
    var firestoreEvent by remember { mutableStateOf<Event?>(null) }


    LaunchedEffect(id) {
        Log.d(TAG, "Loading festival for id: $id")

        db.collection("events")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val event = document.toObject(Event::class.java)
                    // Setze die ID manuell
                    event?.id = document.id
                    firestoreEvent = event
                    Log.d(TAG, "Firestore event loaded: ${event?.title}")
                } else {
                    Log.w(TAG, "Firestore event not found: $id")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading Firestore event", e)
            }

    }

    val event = id

    if (firestoreEvent == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AccentColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TopLightBlue,
                                BottomDarkBlue
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Zurück",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(24.dp)
                            .size(34.dp)
                            .clickable { navController.popBackStack() }
                    )
                }
                Text("Event nicht gefunden", color = Color.White, fontSize = 24.sp)
            }
            return
        }

    }

    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Zu Favoriten hinzugefügt" -> Color(0xFF4CAF50)
                    "Fehler beim laden von Event!" -> Color(0xFFF44336)
                    "Fehler!" -> Color(0xFFF44336)
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

            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Zurück",
                tint = Color.White,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(24.dp)
                    .size(34.dp)
                    .clickable {
                        Log.d(TAG, "Navigation: Returning to previous screen")
                        navController.popBackStack()
                    }
            )

//            Icon(
//                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
//                contentDescription = "Favorite",
//                tint = if (isFavorite) Color.Yellow else Color.White,
//                modifier = Modifier
//                    .align(alignment = Alignment.TopEnd)
//                    .padding(24.dp)
//                    .size(34.dp)
//                    .clickable{
//                        Log.i(TAG, "Favorite clicked for: ${event!!.name.take(15)}...")
//                        viewModel.toggleFavorite(event!!)
//                    }
//            )

            ClickButton(
                text = "Auf der Karte",
                onClick = {
                    Log.d(TAG, "Navigating to location screen")

                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp, start = 120.dp, end = 120.dp)
                    .fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Log.d(TAG, "Rendering content for: ${event}...")

                    // Bild mit AsyncImage direkt laden
                    val imageUrl = firestoreEvent?.imageUrl
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {

                        AsyncImage(
                            model = imageUrl ?: R.drawable.img,
                            contentDescription = "Event Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                    }

                    // Titel
                    Text(
                        text = firestoreEvent?.title ?: "Kein Titel",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(
                        text = "Beschreibung: ${firestoreEvent?.description ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Datum
                    Text(
                        text = "Datum: ${firestoreEvent?.startData ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(
                        text = "Ort: ${firestoreEvent?.city ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 25.sp),
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Spacer für minimale Scroll-Länge
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    )

                }
            }

//            AnyeBottomBar(navController)
        }
    }
}
