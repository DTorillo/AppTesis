package com.example.capilux.screen.mask

import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun MaskPreviewScreen(
    imageUri: String,        // El path o string de la foto original (¡persistente!)
    useAltTheme: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    // Siempre usa el archivo persistente
    val originalFile = File(context.filesDir, "original_usuario.jpg")
    val maskFile = File(context.filesDir, "mascara_tmp.png")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Previsualización de máscara",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto original arriba
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Original", color = Color.White)
                if (originalFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen original",
                            modifier = Modifier.size(160.dp)
                        )
                    }
                } else {
                    Text("❌ Sin imagen", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Máscara generada abajo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Máscara", color = Color.White)
                if (maskFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(maskFile.absolutePath)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Máscara",
                            modifier = Modifier.size(160.dp)
                        )
                    }
                } else {
                    Text("❌ No generada", color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para aceptar máscara y continuar a selección de estilos/cortes
        Button(
            onClick = {
                navController.navigate("promptSelectionScreen/${Uri.encode(originalFile.absolutePath)}")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Aceptar y continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para regenerar la máscara
        Button(
            onClick = {
                navController.navigate("maskProcessingScreen/${Uri.encode(originalFile.absolutePath)}")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Regenerar máscara")
        }
    }
}
