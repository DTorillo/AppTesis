package com.example.capilux.screen

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.network.GenerationApi
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PromptSelectionScreen(faceShape: String, navController: NavHostController, sharedViewModel: SharedViewModel) {
    val prompts = getPrompts(faceShape)
    val loading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona un estilo",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        prompts.forEach { prompt ->
            Button(
                onClick = {
                    val uri = sharedViewModel.imageUri
                    if (uri != null) {
                        loading.value = true
                        sharedViewModel.setSelectedPrompt(prompt)
                        coroutineScope.launch {
                            val file = File(uri.path ?: "")
                            val bytes = GenerationApi.enviarImagenConPrompt(file, prompt)
                            if (bytes != null) {
                                val output = File(file.parentFile, "generated_${System.currentTimeMillis()}.png")
                                output.writeBytes(bytes)
                                val encoded = Uri.encode(output.toURI().toString())
                                navController.navigate("generatedImage/$encoded")
                            } else {
                                loading.value = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(prompt)
            }
        }
        if (loading.value) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

private fun getPrompts(shape: String): List<String> = when (shape.lowercase()) {
    "redondo" -> listOf("Pompadour moderno", "Corte alto y desvanecido", "Flequillo hacia arriba")
    "cuadrado" -> listOf("Crew cut definido", "Corte clásico", "Fade estructurado")
    "ovalado" -> listOf("Estilo europeo", "Peinado lateral elegante", "Cabello con volumen")
    else -> listOf("Estilo libre")
}
