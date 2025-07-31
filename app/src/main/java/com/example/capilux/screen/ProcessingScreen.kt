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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun ProcessingScreen(imageUri: String, useAltTheme: Boolean, navController: NavHostController) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val decodedUri = Uri.parse(imageUri)

    val infiniteTransition = rememberInfiniteTransition(label = "LogoAndHaloAnim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "LogoScale"
    )
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

    // SOLO se llama a la IA de simetría/análisis facial:
    LaunchedEffect(imageUri) {
        try {
            Log.d("Capilux", "🚀 Entrando a ProcessingScreen con URI: $imageUri")
            CapiluxApi.analizarSimetria(
                context = context,
                imageUri = decodedUri,
                onSuccess = { resultado ->
                    Log.d("Capilux", "🎉 Análisis facial completado, resultado recibido")
                    navController.navigate("analysisResult/${Uri.encode(resultado)}") {
                        popUpTo("processing/{imageUri}") { inclusive = true }
                    }
                },
                onError = { mensaje ->
                    Log.e("Capilux", "❌ Error en análisis facial: $mensaje")
                    navController.navigate("errorScreen/${Uri.encode("Error: $mensaje")}")
                }
            )
        } catch (e: Exception) {
            Log.e("Capilux", "❌ Error inesperado en ProcessingScreen: ${e.message}")
            navController.navigate("errorScreen/${Uri.encode("Error interno: ${e.message}")}")
        }
    }

    // UI de carga mientras se analiza:
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
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
                text = "Procesando tu rostro...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Detectando proporciones y simetría facial...",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
