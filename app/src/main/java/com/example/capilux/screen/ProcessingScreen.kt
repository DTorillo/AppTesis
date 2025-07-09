package com.example.capilux.screen

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.network.ServerApi
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun ProcessingScreen(imageUri: String, useAltTheme: Boolean, navController: NavHostController) {
    val context = LocalContext.current
    val processing = remember { mutableStateOf(true) }

    LaunchedEffect(imageUri) {
        processing.value = true
        try {
            val decodedUri = Uri.parse(URLDecoder.decode(imageUri, "UTF-8"))
            val imageFile = File(decodedUri.path ?: "")

            Log.d("ProcessingScreen", "Archivo enviado: ${imageFile.path}")

            ServerApi.enviarImagen(
                context = context,
                imageUri = decodedUri,
                onResult = { response ->
                    try {
                        Log.d("ProcessingScreen", "Respuesta del servidor: $response")

                        val json = JSONObject(response)
                        val resultado = json.optString("resultado")

                        if (resultado.contains("Forma del rostro:")) {
                            val resultadoCodificado = Uri.encode(resultado)
                            navController.navigate("analysisResult/$resultadoCodificado") {
                                popUpTo("processing/{imageUri}") { inclusive = true }
                            }
                        } else {
                            Log.e("ProcessingScreen", "La respuesta no contiene forma válida.")
                            navController.popBackStack()
                        }
                    } catch (e: Exception) {
                        Log.e("ProcessingScreen", "Error al procesar JSON: ${e.message}")
                        navController.popBackStack()
                    }
                },
                onError = { error ->
                    Log.e("ProcessingScreen", "Error al enviar imagen: $error")
                    navController.popBackStack()
                }
            )
        } catch (e: Exception) {
            Log.e("ProcessingScreen", "Excepción general: ${e.message}")
            navController.popBackStack()
        }
        processing.value = false
    }

    // Animación visual de carga
    val gradient = backgroundGradient(useAltTheme)
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        if (processing.value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale.value)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(160.dp)
                        .height(4.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
            }
            Text(
                text = "Procesando análisis facial...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
