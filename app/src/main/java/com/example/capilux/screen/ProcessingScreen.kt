package com.example.capilux.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.navigation.NavHostController
import com.example.capilux.FaceAnalyzer
import com.example.capilux.FaceAnalysisResult
import com.example.capilux.R
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProcessingScreen(imageUri: String, useAltTheme: Boolean, navController: NavHostController) {
    val context = LocalContext.current
    val processing = remember { mutableStateOf(true) }

    LaunchedEffect(imageUri) {
        processing.value = true
        val result = withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUri))
            val bitmap = inputStream?.use { BitmapFactory.decodeStream(it) }
            if (bitmap != null) {
                FaceAnalyzer().analyzeFace(bitmap)
            } else {
                FaceAnalysisResult.Error("No se pudo abrir la imagen")
            }
        }
        processing.value = false
        when (result) {
            is FaceAnalysisResult.Success -> {
                navController.navigate("analysisResult/${result.faceShape}/${result.ratio}") {
                    popUpTo("processing/{imageUri}") { inclusive = true }
                }
            }
            is FaceAnalysisResult.Error -> {
                // En caso de error vuelve a la pantalla principal
                navController.popBackStack()
            }
        }
    }

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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
            )
            Text(
                text = "Cargando",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
