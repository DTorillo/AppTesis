package com.example.capilux.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.navigation.NavHostController
import com.example.capilux.FaceAnalyzer
import com.example.capilux.FaceAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProcessingScreen(imageUri: String, navController: NavHostController) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (processing.value) {
            CircularProgressIndicator(color = Color.White)
            Text(
                text = "Procesando...",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
