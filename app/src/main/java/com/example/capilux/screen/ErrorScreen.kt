package com.example.capilux.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun ErrorScreen(message: String, useAltTheme: Boolean, navController: NavHostController) {
    val gradient = backgroundGradient(useAltTheme)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Oops, ocurrió un error",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Text(
                text = message,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}
