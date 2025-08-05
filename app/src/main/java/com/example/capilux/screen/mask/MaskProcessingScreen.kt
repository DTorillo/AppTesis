package com.example.capilux.screen.mask

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.components.LoadingOverlay
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MaskProcessingScreen(
    imageUri: String, // se ignora, ¡usamos la imagen persistente!
    useAltTheme: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Siempre usa la imagen persistente
    val originalFile = File(context.filesDir, "original_usuario.jpg")
    val decodedUri = Uri.fromFile(originalFile)

    LaunchedEffect(originalFile.absolutePath) {
        if (!originalFile.exists() || originalFile.length() < 10_000) {
            scope.launch(Dispatchers.Main) {
                error.value = "La imagen original no existe o fue eliminada."
                loading.value = false
            }
            return@LaunchedEffect
        }

        CapiluxApi.generarMascara(
            context = context,
            imageUri = decodedUri,
            onSuccess = { bytes ->
                scope.launch(Dispatchers.Main) {
                    try {
                        // Guarda la máscara en almacenamiento interno
                        val file = File(context.filesDir, "mascara_tmp.png")
                        file.writeBytes(bytes)
                        navController.navigate("maskPreviewScreen/${Uri.encode(originalFile.absolutePath)}") {
                            popUpTo("maskProcessingScreen/$imageUri") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        error.value = "No se pudo guardar la máscara: ${e.message}"
                    }
                    loading.value = false
                }
            },
            onError = { mensaje ->
                scope.launch(Dispatchers.Main) {
                    error.value = mensaje
                    loading.value = false
                }
            }
        )
    }

    // UI feedback
    if (loading.value) {
        LoadingOverlay(message = "Generando máscara...", useAltTheme = useAltTheme)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            error.value?.let { mensaje ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = mensaje,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) { Text("Volver") }
                }
            }
        }
    }
}
