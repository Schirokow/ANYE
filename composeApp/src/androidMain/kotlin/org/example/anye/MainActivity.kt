package org.example.anye

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import org.example.anye.data.EventViewModel
import org.example.anye.presentation.screens.AccountScreen
import org.example.anye.presentation.screens.ContentDetailScreen
import org.example.anye.presentation.screens.CreateEventScreen
import org.example.anye.presentation.screens.EventScreen
import org.example.anye.presentation.screens.FavoriteScreen
import org.example.anye.presentation.screens.FirestoreEventDetailScreen
import org.example.anye.presentation.screens.HomeScreen
import org.example.anye.presentation.screens.LocationScreen
import org.example.anye.presentation.screens.LoginScreen
import org.example.anye.presentation.screens.ProfileScreen
import org.example.anye.presentation.screens.RegistrationScreen
import org.example.anye.presentation.screens.SettingScreen
import org.example.anye.viewmodels.HomeViewModel
import org.example.anye.viewmodels.LoginViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Activity created")
        enableEdgeToEdge()
        setContent {
            Log.d(TAG, "Composing UI content")
            Navigation()

        }
    }
}


@Composable
fun Navigation() {

    val TAG = "AppNavigation"
    val viewModel: LoginViewModel = koinViewModel()
    val navController = rememberNavController()

    // Aktuellen User holen
//    val currentUser = Firebase.auth.currentUser

    // Dynamische Startseite abhängig vom User
    val startDestination = if (viewModel.getCurrentUser() == null) {
        "LoginScreen"

    } else {
        "ProfileScreen"
    }


    NavHost(
        navController = navController,
        startDestination = startDestination //Dynamische Startseite
//        startDestination = "LocationScreen"
    ) {
        composable("HomeScreen") {
            Log.d(TAG, "Navigating to HomeScreen")
            HomeScreen(navController)
        }
        composable("SettingScreen") {
            Log.d(TAG, "Navigating to SettingScreen")
            SettingScreen(navController)
        }
        composable("CreateEventScreen") {
            Log.d(TAG, "Navigating to CreateEventScreen")
            CreateEventScreen(navController)
        }
        composable("FavoriteScreen") {
            Log.d(TAG, "Navigating to FavoriteScreen")
            FavoriteScreen(navController)
        }
        composable("LoginScreen") {
            Log.d(TAG, "Navigating to LoginScreen")
            LoginScreen(navController)
        }
        composable(
            // 1. Route definieren, um optionale Argumente anzunehmen
            route = "LocationScreen?lat={lat}&lng={lng}&eventName={eventName}",
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.StringType // Als String übergeben, da sie aus der API als String kommen
                    nullable = true
                },
                navArgument("lng") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("eventName") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            // 2. Argumente auslesen und in Doubles umwandeln
            val eventLat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val eventLng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()

            // Argument auslesen und dekodieren
            val eventName = backStackEntry.arguments?.getString("eventName")
            val decodedEventName = eventName?.let {
                try {
                    URLDecoder.decode(it, "UTF-8")
                } catch (e: Exception) {
                    "Event" // Fallback
                }
            }

            // 3. Die Werte an deinen Screen übergeben
            LocationScreen(
                navController = navController,
                eventLat = eventLat,
                eventLng = eventLng,
                eventName = decodedEventName
            )
        }
        composable("RegistrationScreen") {
            Log.d(TAG, "Navigating to RegistrationScreen")
            RegistrationScreen(navController)
        }

        composable(
            route = "ContentDetailScreen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            Log.d(TAG, "Navigating to ContentDetailScreen with index: $id")
            ContentDetailScreen(navController, id)
        }

        composable("FirestoreEventDetailScreen/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            FirestoreEventDetailScreen(navController, id)
        }

        composable("ProfileScreen") {
            ProfileScreen(navController, null)
        }

        composable("AccountScreen") {
            AccountScreen(navController)
        }

        composable("EventScreen") {
            EventScreen(navController)
        }
    }

}

//Navigationsmechanismus:
//
//{userName}: Platzhalter für Parameter
//navArgument: Definiert den erwarteten Typ
//backStackEntry.arguments: Zugriff auf übergebene Parameter
//Sicherer Zugriff mit Elvis-Operator: ?: "Default"


// Selbstdefinierte Farben für Hintergrund und Vordergrund.
val BackgroundColor = Color(0xFF20587B)
val ForegroundColor = Color(0xFFED6E63)
val AccentColor = Color(0xFF29719E)
val BottomDarkBlue = Color(0xFF1A4D6C)
val TopLightBlue = Color(0xFF62A7C3)
