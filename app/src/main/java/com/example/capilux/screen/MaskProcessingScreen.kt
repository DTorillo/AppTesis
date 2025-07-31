package com.example.capilux.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun MaskProcessingScreen(
    imageUri: String,        // Path o string de la foto original
    useAltTheme: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val uri = Uri.parse(imageUri)

    LaunchedEffect(imageUri) {
        CapiluxApi.generarMascara(
            context = context,
            imageUri = uri,
            onSuccess = { mascaraBytes ->
                // Guarda la máscara como archivo temporal
                val maskFile = File(context.filesDir, "mascara_tmp.png")
                maskFile.writeBytes(mascaraBytes)
                // Navega a la pantalla de comparación/previsualización de máscara
                navController.navigate("maskPreviewScreen/$imageUri")
            },
            onError = { error ->
                Log.e("Capilux", "❌ Error al generar máscara: $error")
                navController.navigate("errorScreen/${Uri.encode(error)}")
            }
        )
    }

    // UI de carga mientras se genera la máscara
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Generando máscara de cabello...",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
