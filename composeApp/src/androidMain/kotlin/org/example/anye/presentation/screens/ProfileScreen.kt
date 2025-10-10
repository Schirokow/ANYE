package org.example.anye.presentation.screens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.ContentScale
import com.example.evoo.ui.components.button.EditIconButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.ui.menu.AnyeBottomBar
import com.example.evoo.ui.theme.colorthemetype.BottomDarkBlue
import com.example.evoo.ui.theme.colorthemetype.TopLightBlue
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import org.example.anye.viewmodels.Profile1ViewModel
import org.example.anye.AccentColor
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.viewmodels.AuthResult
import org.example.anye.viewmodels.LoginViewModel
import org.koin.androidx.compose.koinViewModel



@Composable
fun ProfileScreen (navController: NavController, userId: Int?) {

    val viewModel: LoginViewModel = koinViewModel()
    val user = FirebaseAuth.getInstance().currentUser
    val firestore = Firebase.firestore
    var userName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("username") ?: user.email ?: ""
                }
        }
    }

    // Auf Events vom ViewModel hören
    LaunchedEffect(Unit) {
        viewModel.authResult.collect { result ->
            when (result) {
                is AuthResult.Success -> {
                    snackbarHostState.showSnackbar(message = result.message)
                    // Navigation nach erfolgreichem Logout
                    navController.navigate("LoginScreen") {
                        popUpTo("ProfileScreen") {
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


    val itemsPerRow = 3



    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // Hole Farbe aus den SnackbarData-Extras
                val background = when (data.visuals.message) {
                    "Erfolgreich abgemeldet" -> Color(0xFF4CAF50) // Grün
                    "E-Mail oder Passwort ist falsch" -> Color(0xFFF44336) // Rot
                    else -> Color(0xFF2196F3) // Blau (Standard)
                }
                androidx.compose.material3.Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    containerColor = background,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
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
                        colors = listOf(TopLightBlue, BottomDarkBlue)
                    )
                )
        ) {
            // Auth-Status oben rechts
            AuthStatusIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
            )
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Zurück",
                tint = Color.White,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(24.dp)
                    .size(34.dp)
                    .clickable {
                        Log.d("ProfileScreen", "Navigation: Returning to previous screen")
                        navController.popBackStack()
                    }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(50.dp))

                // NameFeld
                Text(
                    text = "Willkommen $userName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )


                Spacer(modifier = Modifier.height(20.dp))


                //Profilbild mit Edit
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = org.example.anye.R.drawable.default_avatar),
                        contentDescription = "ProfilePicture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = 36.dp, y = -24.dp)
                ) {
                    EditIconButton(
                        modifier = Modifier
                            .size(24.dp)

                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                ClickButton(
                    text = "Edit Profile",
                    onClick = {
                        navController.navigate("AccountScreen")
                    },
                    modifier = Modifier
                )

                ClickButton(
                    text = "Abmelden",
                    onClick = {
                        viewModel.signOut()
//                        FirebaseAuth.getInstance().signOut()
//                        navController.navigate("LoginScreen") {
//                            popUpTo("ProfileScreen") { inclusive = true }
//                        }
                    }
                )

//                TabRow(
//                    selectedTabIndex = selectedTab.ordinal,
//                    modifier = Modifier.fillMaxWidth(),
//                    containerColor = Color.Transparent
//                ) {
//                    EventTab.entries.forEach { tab ->
//                        Tab(
//                            selected = selectedTab == tab,
//                            onClick = {
//                                Log.d(TAG, "Tab changed to: ${tab.name}")
//                                selectedTab = tab
//                                      },
//                            icon = {
//                                Icon(
//                                    imageVector = tab.icon,
//                                    contentDescription = tab.label,
//                                    tint = if (selectedTab == tab) Color.White else Color.DarkGray
//                                )
//                            }
//                        )
//                    }
//                }
                //HorizontalDivider(
                //  color = Color.LightGray,
                //thickness = 5.dp,
                //modifier = Modifier.padding(vertical = 8.dp)
                //)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(itemsPerRow),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
//                    items(displayedEvents) { event ->
//                        EventCard(
//                            event = event,
//                            onClick = {
//                                Log.d(TAG, "Event clicked: ${event.title.take(15)}...")
//                            },
//                            isLarge = true,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .aspectRatio(1f)
//                        )
//                    }
                }
            }
            AnyeBottomBar(navController)
        }

    }


}