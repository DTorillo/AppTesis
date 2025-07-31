package com.example.capilux.screen

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
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun MaskPreviewScreen(
    imageUri: String,        // El path o string de la foto original
    useAltTheme: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val originalFile = File(Uri.decode(imageUri))
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // Foto original
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Original", color = Color.White)
                if (originalFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen original",
                        modifier = Modifier.size(160.dp).padding(8.dp)
                    )
                } else {
                    Text("❌ Sin imagen", color = Color.Red)
                }
            }

            // Máscara generada
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Máscara", color = Color.White)
                if (maskFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(maskFile.absolutePath)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Máscara",
                        modifier = Modifier.size(160.dp).padding(8.dp)
                    )
                } else {
                    Text("❌ No generada", color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para aceptar máscara y continuar a selección de estilos/cortes
        Button(
            onClick = {
                navController.navigate("promptSelectionScreen/$imageUri")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Aceptar y continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para regenerar la máscara
        Button(
            onClick = {
                // Vuelve a la pantalla de generación de máscara (esto la regenera)
                navController.navigate("maskProcessingScreen/$imageUri")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Regenerar máscara")
        }
    }
}
