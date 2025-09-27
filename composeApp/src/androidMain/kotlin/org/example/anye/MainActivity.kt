package org.example.anye

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.example.anye.presentation.screens.ContentDetailScreen
import org.example.anye.presentation.screens.HomeScreen
import org.example.anye.presentation.screens.LocationScreen
import org.example.anye.presentation.screens.LoginScreen
import org.example.anye.presentation.screens.ProfileScreen1
import org.example.anye.presentation.screens.RegistrationScreen
import org.example.anye.presentation.screens.FavoriteScreen
import org.example.anye.presentation.screens.SettingScreen

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
        // Firebase Test
//        val fs = Firebase.firestore
//        fs.collection("users").document().set(mapOf("name" to "Testname"))
    }
}

@Preview
@Composable
fun Navigation() {

    val TAG = "AppNavigation"
    val navController = rememberNavController()

    fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    // Aktuellen User holen
//    val currentUser = Firebase.auth.currentUser
    val currentUser = getCurrentUser()

    // Dynamische Startseite abhängig vom User
    val startDestination = if (getCurrentUser() == null) {
        "LoginScreen"
    } else {
        "LocationScreen"
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
        composable("FavoriteScreen") {
            Log.d(TAG, "Navigating to FavoriteScreen")
            FavoriteScreen(navController)
        }
        composable("LoginScreen") {
            Log.d(TAG, "Navigating to LoginScreen")
            LoginScreen(navController)
        }
        composable("LocationScreen") {
            Log.d(TAG, "Navigating to LocationScreen")
            LocationScreen(navController)
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

        composable(
            "ProfileScreen1/{userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            Log.d(TAG, "Navigating to ProfileScreen1 for user with id: $userId")
            ProfileScreen1(navController, userId)
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
