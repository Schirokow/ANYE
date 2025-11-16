package org.example.anye.presentation.screens

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.viewmodels.LoginViewModel
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.viewmodels.AuthResult
import org.koin.androidx.compose.koinViewModel

const val TAG = "RegistrationScreen"

@Composable
fun RegistrationScreen(navController: NavController) {

    val auth = Firebase.auth
    val viewModel: LoginViewModel = koinViewModel()

    // Zustand für den Snackbar erstellen
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var userNameState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var repeatPasswordState by remember { mutableStateOf("") }

    // Zustand für den Bestätigungsdialog
    var showDeleteDialog by remember { mutableStateOf(false) }


    // Auf Events vom ViewModel hören
    LaunchedEffect(Unit) {
        viewModel.authResult.collect { result ->
            when (result) {
                is AuthResult.Success -> {
                    snackbarHostState.showSnackbar(message = result.message)
                    // Navigation nach erfolgreicher Registrierung
                    navController.navigate("ProfileScreen") {
                        popUpTo("RegistrationScreen") {
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
                        "Registrierung erfolgreich!" -> Color(0xFF4CAF50) // Grün
                    in listOf(
                        "Fehler bei der Registrierung",
                        "Passwörter stimmen nicht überein"
                    ) -> Color(0xFFF44336) // Rot
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "ArrowBack",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
                        .size(34.dp)
                        .clickable {
                            Log.d(TAG, "Navigating to LoginScreen")
                            navController.navigate("LoginScreen")
                        }

                )
                Text(
                    text = "Login",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clickable {
                            Log.d(TAG, "Navigating to LoginScreen")
                            navController.navigate("LoginScreen")
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
                    "Event Up Your Life",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedTextField(
                    value = userNameState,
                    onValueChange = { userNameState = it },
//                    label = { Text("Benutzername") },
                    placeholder = { Text("Benutzername", color = Color.White) },
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
                    value = emailState,
                    onValueChange = {
                        emailState = it
                    },
//                    label = { Text("E-Mail") },
                    placeholder = { Text("E-Mail", color = Color.White) },
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
                    value = passwordState,
                    onValueChange = {
                        passwordState = it
                    },
//                    label = { Text("Passwort") },
                    placeholder = { Text("Passwort", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
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
                    value = repeatPasswordState,
                    onValueChange = { repeatPasswordState = it },
//                    label = { Text("Passwort wiederholen") },
                    placeholder = { Text("Passwort wiederholen", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
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


                ClickButton(
                    text = "Registrieren",
                    modifier = Modifier
                        .padding(horizontal = 120.dp)
                        .fillMaxWidth(),
                    onClick = {
                        Log.d(TAG, "Attempting registration...")
//                        signUp(auth, emailState, passwordState)
                        if (emailState.isNotBlank() && passwordState.isNotBlank() && userNameState.isNotBlank() && repeatPasswordState.isNotBlank() && passwordState == repeatPasswordState) {
                            viewModel.signUp(emailState, passwordState, userNameState)
//                            userNameState = ""
//                            emailState = ""
//                            passwordState = ""
//                            repeatPasswordState = ""
                        } else if (passwordState != repeatPasswordState) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwörter stimmen nicht überein")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Bitte alle Felder ausfühlen!")
                            }
                        }

                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

//                ClickButton(
//                    text = "Account löschen",
//                    onClick = {
//                        showDeleteDialog = true
//                    },
//                    modifier = Modifier
//                        .padding(horizontal = 120.dp)
//                        .fillMaxWidth()
//                )

                // AlerDer Bestätigungsdialog
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                "Account löschen?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Text(
                                "Bist du sicher, dass du deinen Account unwiderruflich löschen möchtest? Deine Daten werden dauerhaft entfernt.",
                                color = Color.White
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteAccount(emailState, passwordState)
                                    userNameState = ""
                                    emailState = ""
                                    passwordState = ""
                                    repeatPasswordState = ""
                                }
                            ) {
                                Text("Ja, löschen", color = Color.White)
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDeleteDialog = false },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("Abbrechen", color = Color.White)
                            }
                        },
                        containerColor = Color(0xFF1E1E1E),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }
            }
        }


    }
    //MenuBar(navController)
}





