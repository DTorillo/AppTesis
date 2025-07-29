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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun MaskPreviewScreen(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme = true)
    val originalFile = sharedViewModel.imageUri?.path?.let { File(it) }
    val maskFile = File(context.filesDir, "mascara.png")

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Original", color = Color.White)
                originalFile?.takeIf { it.exists() }?.let {
                    val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Imagen original",
                        modifier = Modifier
                            .size(160.dp)
                            .padding(8.dp)
                    )
                } ?: Text("❌ Sin imagen", color = Color.Red)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Máscara", color = Color.White)
                if (maskFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(maskFile.absolutePath)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Máscara",
                        modifier = Modifier
                            .size(160.dp)
                            .padding(8.dp)
                    )
                } else {
                    Text("❌ No generada", color = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate("promptSelection/${sharedViewModel.faceShape ?: ""}")
        }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Aceptar y continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("processing/${sharedViewModel.imageUri.toString()}")
        }, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Regenerar máscara")
        }
    }
}
