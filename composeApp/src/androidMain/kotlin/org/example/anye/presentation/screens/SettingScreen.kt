package org.example.anye.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.example.anye.AccentColor
import org.example.anye.BottomDarkBlue
import org.example.anye.TopLightBlue
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.menu.AnyeBottomBar

@Composable
fun SettingScreen(navController: NavController){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentColor)
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()) // Eine Function um den Content unter der Status Bar anzuzeigen.
                .background(brush = Brush.verticalGradient(colors = listOf(
                    TopLightBlue,
                    BottomDarkBlue
                )))
        ){
             //Auth-Status oben rechts
            AuthStatusIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )


            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Zurück",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(34.dp)
                        .clickable { navController.popBackStack() }
                )
                Spacer(modifier = Modifier.width(70.dp))
                Text("Settings",
                    fontSize = 30.sp, color = Color.White,
                )
            }

            // Menu Bar
            //MenuBar(navController)
            AnyeBottomBar(navController)
        }
    }
}


