package com.example.capilux.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs

@Composable
fun SplashDecisionScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val sharedPrefs = EncryptedPrefs.get(context)

    val username = sharedPrefs.getString("username", null)
    var showDialog by remember { mutableStateOf(username == null) }

    val gradient = backgroundGradient(useAltTheme)

    // Si ya hay usuario → login
    LaunchedEffect(Unit) {
        if (username != null) {
            navController.navigate("login") {
                popUpTo("splashDecision") { inclusive = true }
            }
        }
    }

    // Si no hay usuario, muestra splash con opción biométrica
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Bienvenido a Capilux",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { /* No se puede cerrar */ },
                title = { Text("¿Quieres usar huella digital?", color = Color.White) },
                text = { Text("Puedes activar la huella para proteger tu app.", color = Color.White) },
                confirmButton = {
                    TextButton(onClick = {
                        sharedPrefs.edit().putBoolean("useBiometric", true).apply()
                        navController.navigate("userCreation") {
                            popUpTo("splashDecision") { inclusive = true }
                        }
                    }) {
                        Text("Sí", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        sharedPrefs.edit().putBoolean("useBiometric", false).apply()
                        navController.navigate("userCreation") {
                            popUpTo("splashDecision") { inclusive = true }
                        }
                    }) {
                        Text("No", color = Color.White)
                    }
                },
                containerColor = Color(0xFF2D0C5A)
            )
        }
    }
}
