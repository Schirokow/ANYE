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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import org.example.anye.ui.components.buttons.ClickButton
import org.example.anye.data.User
import org.example.anye.viewmodels.LoginViewModel
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.koin.androidx.compose.koinViewModel

private const val TAG = "RegistrationScreen"

@Composable
fun RegistrationScreen(navController: NavController) {

    val auth = Firebase.auth

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
                        .clickable{
                            Log.d(TAG, "Navigating to LoginScreen")
                            navController.navigate("LoginScreen")
                        }
                )
            }

            val viewModel: LoginViewModel = koinViewModel()
            val userData by viewModel.users.collectAsState()

            val userNameState = remember { mutableStateOf(TextFieldValue()) }
            var emailState by remember { mutableStateOf("") }
            var passwordState by remember { mutableStateOf("") }
            var repeatPasswordState by remember { mutableStateOf("") }

            var registrationError by remember { mutableStateOf<RegistrationError?>(null) }

            if (registrationError != null) {
                Log.w(TAG, "Registration error: ${registrationError?.name} - ${registrationError?.message}")
                AlertDialog(
                    onDismissRequest = { registrationError = null },
                    title = { Text("Registrierungsfehler") },
                    text = { Text(registrationError!!.message) },
                    confirmButton = {
                        Button(onClick = {
                            Log.d(TAG, "Dismissed error dialog: ${registrationError?.name}")
                            registrationError = null
                        }) {
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

                Spacer(modifier = Modifier.height(30.dp))

                TextField(
                    value = userNameState.value,
                    onValueChange = { newText -> userNameState.value = newText },
//                    label = { Text("Benutzername") },
                    placeholder = { Text("Benutzername") },
                    singleLine = true,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )

                TextField(
                    value = emailState,
                    onValueChange = {
                        emailState = it
                    },
//                    label = { Text("E-Mail") },
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
//                    label = { Text("Passwort") },
                    placeholder = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                )
                TextField(
                    value = repeatPasswordState,
                    onValueChange = { repeatPasswordState = it },
//                    label = { Text("Passwort wiederholen") },
                    placeholder = { Text("Passwort wiederholen") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
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
                        signUp(auth, emailState, passwordState)
                        emailState = ""
                        passwordState = ""
                        repeatPasswordState = ""

                        when {
                            // Validierungen

//                            userNameState.value.text.isBlank() ->
//                                registrationError = RegistrationError.EMPTY_USERNAME
                            emailState.isBlank() ->
                                registrationError = RegistrationError.EMPTY_EMAIL
                            passwordState.isBlank() ->
                                registrationError = RegistrationError.EMPTY_PASSWORD
                            passwordState != repeatPasswordState ->
                                registrationError = RegistrationError.PASSWORD_MISMATCH
//                            userData.any { it.email == user.email } ->
//                                registrationError = RegistrationError.EMAIL_EXISTS
//                            userData.any { it.name == user.name } ->
//                                registrationError = RegistrationError.USERNAME_EXISTS
//                            !isValidEmail(user.email) ->
//                                registrationError = RegistrationError.INVALID_EMAIL
                            else -> {
                                // Registrierung erfolgreich
                                Log.i(TAG, "Registration successful for user Id:")
//                               viewModel.addUser(user) // Fügt Benutzer hinzu
//                                AuthManager.login(user) // Automatischer Login
                                navController.navigate("ProfileScreen1/$") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                ClickButton(
                    text = "Abbrechen",
                    onClick = {
                        Log.d(TAG, "Registration cancelled, navigating to login")
                        navController.navigate("LoginScreen")
                              },
                    modifier = Modifier
                        .padding(horizontal = 120.dp)
                        .fillMaxWidth()
                )
            }
            //MenuBar(navController)
        }

    }

}


private fun signUp(auth: FirebaseAuth, email: String, password: String){
    auth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener {
            if (it.isSuccessful){
                Log.d("MyLog", "Sign Up successful")
            }else{
                Log.d("MyLog", "Sign Up failure")
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

private fun deleteAccount(auth: FirebaseAuth, email: String, password: String){
    val credential = EmailAuthProvider.getCredential(email,password)
    auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
        if (it.isSuccessful){
            auth.currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful){
                    Log.d("MyLog", "Account was deleted")
                }else{
                    Log.d("MyLog", "Failure delete account")
                }
            }
        }else{
            Log.d("MyLog", "Failure reauthenticate")
        }
    }
}


private fun signOut(auth: FirebaseAuth){
    auth.signOut()
}





// Hilfsfunktion für E-Mail-Validierung mit Regex
private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches() //vordefinierte Regex-Muster Patterns.EMAIL_ADDRESS
}
////Die Funktion isValidEmail verwendet ein Regex-Muster, um das Format der E-Mail zu prüfen.
////Nur wenn das Muster passt, wird die Registrierung fortgesetzt.
//
//// Fehlertypen
enum class RegistrationError(val message: String) {
    EMPTY_USERNAME("Bitte Benutzernamen eingeben"),
    EMPTY_EMAIL("Bitte E-Mail-Adresse eingeben"),
    EMPTY_PASSWORD("Bitte Passwort eingeben"),
    PASSWORD_MISMATCH("Passwörter stimmen nicht überein"),
    EMAIL_EXISTS("E-Mail-Adresse bereits registriert"),
    USERNAME_EXISTS("Benutzername bereits vergeben"),
    INVALID_EMAIL("Ungültiges E-Mail-Format")
}

//Registrierungsflow:
//
//Prüfung aller Eingabefelder
//Eindeutigkeitsprüfung von Email/Benutzername
//Passwortmatch-Check
//E-Mail-Formatvalidierung mit Android-internem Pattern
//
//Bei Erfolg:
//Benutzer wird Repository hinzugefügt
//Automatische Anmeldung
//Persistenz durch saveUsers()