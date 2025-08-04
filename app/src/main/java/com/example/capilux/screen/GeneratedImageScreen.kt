package com.example.capilux.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.saveImageToGallery
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

    // Decodificar la ruta recibida
    val decodedPath = Uri.decode(imageUri)
    val file = File(decodedPath)

    // Logs para depuración
    println("🖼 Pantalla GeneratedImageScreen")
    println("📂 Path recibido: $decodedPath")
    println("📦 Existe archivo: ${file.exists()}, Tamaño: ${if (file.exists()) file.length() else 0} bytes")

    val promptVisible = sharedViewModel.selectedPrompt ?: "Estilo generado"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar nombre del estilo
        Text(
            text = "Estilo aplicado: $promptVisible",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (file.exists() && file.length() > 1000) {
            // Cargar y mostrar imagen si es válida
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp)
                )
            } else {
                Text("❌ No se pudo decodificar la imagen", color = Color.Red)
            }
        } else {
            Text("❌ No se encontró la imagen generada", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                sharedViewModel.clearAll()
                File(context.filesDir, "original_usuario.jpg").delete()
                File(context.filesDir, "mascara_tmp.png").delete()
                File(context.filesDir, "resultado_sd.png").delete()
                navController.navigate("camera")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a tomar otra foto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (file.exists()) {
                    saveImageToGallery(context, file)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar imagen")
        }
    }
}
