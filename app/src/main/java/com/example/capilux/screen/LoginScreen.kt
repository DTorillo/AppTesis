package com.example.capilux.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun LoginScreen(navController: NavHostController, useAltTheme: Boolean) {
    val gradient = backgroundGradient(useAltTheme)
    var pin by remember { mutableStateOf("") }
    val pinCorrecto = "1234"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ingresa tu PIN",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 22.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .width(200.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (pin == pinCorrecto) {
                        navController.navigate("main")
                    } else {
                        pin = ""
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text(text = "Acceder", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¿Olvidaste tu PIN?",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
