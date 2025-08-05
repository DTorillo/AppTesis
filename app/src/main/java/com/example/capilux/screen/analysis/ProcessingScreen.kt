package com.example.capilux.screen.analysis

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.network.CapiluxApi
import com.example.capilux.components.LoadingOverlay
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

    LoadingOverlay(message = "Analizando tu rostro...", useAltTheme = useAltTheme)
}
