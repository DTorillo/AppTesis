package com.example.capilux.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.network.CapiluxApi
import com.example.capilux.utils.saveImageToGallery
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.SecondaryButton
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GeneratedImageScreen(
    imageUri: String,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    useAltTheme: Boolean
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val file = File(context.filesDir, "resultado_sd.png")
    val promptVisible = sharedViewModel.selectedPrompt ?: "Estilo generado"
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 🔹 Controlar el botón físico / gesto de retroceso
    BackHandler {
        navController.navigate("main") {
            popUpTo("main") { inclusive = false }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Mostrar nombre del estilo
        Text(
            text = "Estilo aplicado: $promptVisible",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        } else {
            Text("❌ No se encontró la imagen generada", color = Color.Red)
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        SecondaryButton(
            text = "Volver al inicio",
            onClick = {
                sharedViewModel.clearAll()
                File(context.filesDir, "original_usuario.jpg").delete()
                File(context.filesDir, "mascara_tmp.png").delete()
                File(context.filesDir, "resultado_sd.png").delete()
                navController.navigate("main") {
                    popUpTo("main") { inclusive = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(
            text = "Guardar imagen",
            onClick = { saveImageToGallery(context, file) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(
            text = "Regenerar resultado",
            onClick = {
                val imageFile = File(context.filesDir, "original_usuario.jpg")
                val maskFile = File(context.filesDir, "mascara_tmp.png")
                val prompt = promptVisible
                if (!imageFile.exists() || !maskFile.exists()) {
                    errorMessage = "No se encontró la imagen o la máscara original."
                    return@PrimaryButton
                }
                loading = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        CapiluxApi.generarEstilo(
                            context = context,
                            imageUri = Uri.fromFile(imageFile),
                            mascaraFile = maskFile,
                            prompt = prompt,
                            onSuccess = { resultado ->
                                file.writeBytes(resultado)
                                errorMessage = null
                            },
                            onError = { mensaje ->
                                errorMessage = "Error al regenerar: $mensaje"
                            }
                        )
                    } catch (e: Exception) {
                        errorMessage = "Error inesperado: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Regenerando imagen...", color = Color.White)
                }
            }
        }
    }
}
