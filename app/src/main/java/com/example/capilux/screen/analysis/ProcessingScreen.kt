package com.example.capilux.screen.analysis

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.SharedViewModel
import com.example.capilux.components.AdvancedLoadingOverlay
import com.example.capilux.network.CapiluxApi
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AdvancedLoadingOverlay(
            message = "Analizando tu rostro...",
            subMessage = "Estamos calculando las proporciones faciales\npara recomendarte los mejores cortes",
            useAltTheme = useAltTheme,
            logo = painterResource(id = R.drawable.logo) // Pasa el logo aquí
        )
    }
}