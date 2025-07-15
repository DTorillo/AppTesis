package com.example.capilux.screen

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.network.ServerApi
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URLDecoder

@Composable
fun ProcessingScreen(imageUri: String, useAltTheme: Boolean, navController: NavHostController) {
    val context = LocalContext.current
    val processing = remember { mutableStateOf(true) }
    val gradient = backgroundGradient(useAltTheme)
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "LogoAndHaloAnim")

    // Escala del logo (pulso suave)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "LogoScale"
    )

    // Halo animado
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "HaloAlpha"
    )

    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "HaloScale"
    )

    LaunchedEffect(imageUri) {
        try {
            processing.value = true
            val decodedUri = Uri.parse(URLDecoder.decode(imageUri, "UTF-8"))
            val imageFile = File(decodedUri.path ?: "")

            if (!imageFile.exists()) {
                val msg = Uri.encode("La imagen no se encontró o fue eliminada")
                navController.navigate("errorScreen/$msg")
                return@LaunchedEffect
            }

            ServerApi.enviarImagen(
                context = context,
                imageUri = decodedUri,
                onResult = { response ->
                    coroutineScope.launch {
                        try {
                            val json = JSONObject(response)
                            val resultado = json.optString("resultado", "")
                            val error = json.optString("error", "")

                            if (error.isNotBlank()) {
                                val msg = Uri.encode("Error del servidor: $error")
                                navController.navigate("errorScreen/$msg")
                                return@launch
                            }

                            if (resultado.isNotBlank()) {
                                val resultadoCodificado = Uri.encode(resultado)
                                navController.navigate("analysisResult/$resultadoCodificado") {
                                    popUpTo("processing/{imageUri}") { inclusive = true }
                                }
                            } else {
                                val msg = Uri.encode("Respuesta inválida del servidor")
                                navController.navigate("errorScreen/$msg")
                            }
                        } catch (e: Exception) {
                            val msg = Uri.encode("Error procesando JSON: ${e.message}")
                            navController.navigate("errorScreen/$msg")
                        }
                    }
                },
                onError = { error ->
                    coroutineScope.launch {
                        val msg = Uri.encode("No se pudo conectar al servidor: $error")
                        navController.navigate("errorScreen/$msg")
                    }
                }
            )
        } catch (e: Exception) {
            val msg = Uri.encode("Error interno: ${e.message}")
            navController.navigate("errorScreen/$msg")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Halo animado
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(haloScale)
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = haloAlpha),
                        radius = size.minDimension / 2f
                    )
                }

                // Logo animado
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(180.dp)
                    .height(5.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Analizando rasgos faciales...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cada rostro es único. Calculando proporciones y armonía...",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
