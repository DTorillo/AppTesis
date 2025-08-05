package com.example.capilux.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.SharedViewModel
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.File

@Composable
fun ProcessingScreen(
    imageUri: String,
    useAltTheme: Boolean,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    // ⚡️ Usa siempre la imagen persistente (almacenamiento privado)
    val originalFile = File(context.filesDir, "original_usuario.jpg")
    val decodedUri = Uri.fromFile(originalFile)

    LaunchedEffect(originalFile.absolutePath) {
        if (!originalFile.exists() || originalFile.length() < 10_000) {
            navController.navigate(
                "errorScreen/${Uri.encode("No se encontró la imagen original. Por favor, vuelve a tomar o seleccionar una foto.")}"
            )
            return@LaunchedEffect
        }
        try {
            val resultado = withTimeout(20_000) {
                CapiluxApi.analizarSimetria(context, decodedUri)
            }
            Log.d("Capilux", "🚀 Análisis completado.")
            sharedViewModel.updateImageUri(decodedUri)
            sharedViewModel.updateAnalysisResult(resultado)
            navController.navigate("analysisResult") {
                popUpTo("processing/$imageUri") { inclusive = true }
            }
        } catch (e: TimeoutCancellationException) {
            navController.navigate(
                "errorScreen/${Uri.encode("El análisis tardó demasiado, inténtalo nuevamente")}"
            )
        } catch (e: Exception) {
            navController.navigate(
                "errorScreen/${Uri.encode("Error: ${e.message}")}"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(140.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(180.dp)
                        .height(5.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Procesando tu rostro...",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
