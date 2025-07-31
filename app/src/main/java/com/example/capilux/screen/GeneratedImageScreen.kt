package com.example.capilux.screen

import android.graphics.BitmapFactory
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
import com.example.capilux.utils.saveImageToGallery
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun GeneratedImageScreen(
    imageUri: String,             // <--- Debe llamarse imageUri para que no falle
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    useAltTheme: Boolean
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val file = File(context.filesDir, "resultado_sd.png")
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

        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
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
            Text("❌ No se encontró la imagen generada", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                sharedViewModel.clear()
                navController.navigate("camera") // cambia por tu ruta real de cámara
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a tomar otra foto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                saveImageToGallery(context, file)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar imagen")
        }
    }
}
