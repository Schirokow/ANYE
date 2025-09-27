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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.ui.menu.AnyeBottomBar
import org.example.anye.viewmodels.LoginViewModel
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.shared.R
import org.koin.androidx.compose.koinViewModel

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(navController: NavController) {

    val auth = Firebase.auth
    val currentUser = auth.currentUser?.email
    Log.d(" Authentication", "User email: ${auth.currentUser?.email}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()) // Eine Function um den Content unter der Status Bar anzuzeigen.
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TopLightBlue,
                            BottomDarkBlue
                        )
                    )
                )
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Zurück",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 24.dp, bottom = 24.dp)
                        .size(34.dp)
                        .clickable {
                            Log.d(TAG, "Navigating to registration screen")
                            navController.navigate("RegistrationScreen")
                        }
                )

                Text(
                    text = "Neues Konto",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clickable{
                            Log.d(TAG, "Navigating to registration screen")
                            navController.navigate("RegistrationScreen")
                        }
                )
            }

            val viewModel: LoginViewModel = koinViewModel()

            val userData by viewModel.users.collectAsState()


            // State-Management mit Jetpack Compose
            var emailState by remember { mutableStateOf("") }
            var passwordState by remember { mutableStateOf("") }

            var loginError by remember { mutableStateOf(false) }
            if (loginError) {
                Log.w(TAG, "Showing login error dialog")
                AlertDialog(
                    onDismissRequest = {
                        Log.d(TAG, "Dismissed login error dialog")
                        // Schließt den Dialog wenn der Benutzer außerhalb tippt
                        loginError = false
                    },
                    title = {
                        Text(
                            text = "Fehler bei der Anmeldung",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text("E-Mail oder Passwort ist falsch!")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                Log.d(TAG, "User acknowledged login error")
                                loginError = false
                            }
                        ) {
                            Text("OK")
                        }
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

                Spacer(modifier = Modifier.height(60.dp))

                TextField(
                    value = emailState,
                    onValueChange = {
                        emailState = it
                    },
                    placeholder = { Text("E-Mail") },
                    singleLine = true,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                TextField(
                    value = passwordState,
                    onValueChange = {
                        passwordState = it
                    },
                    placeholder = { Text("Passwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Passwort vergessen?",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 150.dp)
                        .clickable{/* TODO */}
                )
                Spacer(modifier = Modifier.height(50.dp))

                ClickButton(
                    text = "Anmelden",
                    onClick = {
//                        signIn(auth, emailState, passwordState)
                        viewModel.signIn(emailState,passwordState)
                        Log.d(" Authentication", "User email: ${auth.currentUser?.email}")

                    },
                    modifier = Modifier
                        .padding(horizontal = 120.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))

                ClickButton(
                    text = "Abmelden",
                    onClick = {
                        signOut(auth)
                        Log.d(" Authentication", "User email: ${auth.currentUser?.email}")

                    },
                    modifier = Modifier
                        .padding(horizontal = 120.dp)
                        .fillMaxWidth()
                )


            }

            AnyeBottomBar(navController)
        }

    }
}


private fun signIn(auth: FirebaseAuth, email: String, password: String){
    auth.signInWithEmailAndPassword(email,password)
        .addOnCompleteListener {
            if (it.isSuccessful){
                Log.d("MyLog", "Sign In successful")
            }else{
                Log.d("MyLog", "Sign In failure")
            }
        }
}

private fun signOut(auth: FirebaseAuth){
    auth.signOut()
}

//Anmeldeflow:
//
//Nutzer gibt Email/Passwort ein
//Button-Click löst Suche in userData aus
//
//Bei Erfolg:
//AuthManager aktualisiert globalen State
//Navigation zum Profil mit Username als Parameter
//Back-Stack wird gelöscht (kein Zurück zum Login)
//Bei Fehler: AlertDialog wird angezeigt