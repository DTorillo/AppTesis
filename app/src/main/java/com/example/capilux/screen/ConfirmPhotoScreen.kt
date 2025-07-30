package com.example.capilux.screen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.SharedViewModel
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.network.CapiluxApi
import java.io.File

@Composable
fun ConfirmPhotoScreen(
    imageUri: String,
    useAltTheme: Boolean,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val uri = Uri.parse(imageUri)
    val showDialog = remember { mutableStateOf(false) }

    // Convertir y guardar archivo temporal
    var tempFilePath by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(imageUri) {
        try {
            val tempFile = CapiluxApi.uriToFile(context, uri)
            Log.d("Capilux", "📸 Archivo temporal creado en ConfirmPhotoScreen: ${tempFile.absolutePath}")
            tempFilePath = tempFile.absolutePath
        } catch (e: Exception) {
            Log.e("Capilux", "❌ Error copiando imagen: ${e.message}")
            tempFilePath = null
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿La foto está bien?",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Volver a tomar")
                }
                Button(onClick = {
                    if (tempFilePath == null || !File(tempFilePath!!).exists()) {
                        showDialog.value = true
                    } else {
                        // Ya NO uses SharedViewModel para guardar un Uri
                        // Navega pasando el path absoluto como String (URL encoded)
                        navController.navigate("processing/${Uri.encode(tempFilePath!!)}") {
                            popUpTo("confirmPhoto/{imageUri}") { inclusive = true }
                        }
                    }
                }) {
                    Text("Siguiente")
                }
            }
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Error") },
            text = { Text("No se encontró la imagen, vuelve a tomarla o selecciona otra.") },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }
}
