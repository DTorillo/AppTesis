package com.example.capilux.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.SharedViewModel
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.launch
import java.io.File

data class PromptOpcion(
    val nombreVisible: String,
    val promptTecnico: String
)

@Composable
fun PromptSelectionScreen(
    faceShape: String,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    useAltTheme: Boolean
) {
    val prompts = getPrompts(faceShape)
    val loading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    // Siempre usa la imagen persistente
    val imageFile = File(context.filesDir, "original_usuario.jpg")
    val maskFile = File(context.filesDir, "mascara_tmp.png")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Estilos recomendados para rostro $faceShape",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        prompts.forEach { opcion ->
            PrimaryButton(
                onClick = {
                    if (!imageFile.exists() || !maskFile.exists()) {
                        val error = Uri.encode("No se encontró la imagen o la máscara generada.")
                        navController.navigate("errorScreen/$error")
                        return@PrimaryButton
                    }
                    coroutineScope.launch {
                        loading.value = true
                        try {
                            CapiluxApi.generarEstilo(
                                context = context,
                                imageUri = Uri.fromFile(imageFile),
                                mascaraFile = maskFile,
                                prompt = opcion.promptTecnico,
                                onSuccess = { resultado ->
                                    val resultFile = File(context.filesDir, "resultado_sd.png")
                                    resultFile.writeBytes(resultado)

                                    sharedViewModel.updateSelectedPrompt(opcion.nombreVisible)

                                    val encodedPath = Uri.encode(resultFile.absolutePath)
                                    // 🔹 Navegar directamente al resultado y limpiar el backstack
                                    navController.navigate("generatedImage/$encodedPath") {
                                        popUpTo("main") { inclusive = false }
                                    }
                                },
                                onError = { mensaje ->
                                    val encodedMsg = Uri.encode("Error: $mensaje")
                                    navController.navigate("errorScreen/$encodedMsg")
                                }
                            )

                        } catch (e: Exception) {
                            val error = Uri.encode("Error inesperado: ${e.message}")
                            navController.navigate("errorScreen/$error")
                        } finally {
                            loading.value = false
                        }
                    }
                },
                text = opcion.nombreVisible,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        if (loading.value) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

fun getPrompts(faceShape: String): List<PromptOpcion> {
    return when (faceShape.uppercase()) {
        "OVALADO" -> listOf(
            PromptOpcion("Corte con volumen arriba", "modern volumized haircut, clean sides, studio light"),
            PromptOpcion("Peinado lateral elegante", "elegant side part haircut, sharp look, natural lighting"),
            PromptOpcion("Fade alto con textura", "high textured fade, stylish look, hair texture focus")
        )
        "CUADRADO" -> listOf(
            PromptOpcion("Crew cut definido", "crew cut, masculine clean style, sharp sides"),
            PromptOpcion("Buzz cut", "buzz cut, military hairstyle, clean head, modern lighting")
        )
        "REDONDO" -> listOf(
            PromptOpcion("Pompadour moderno", "modern pompadour, stylish top volume, fade sides"),
            PromptOpcion("Flequillo hacia arriba", "fringe up hairstyle, edgy look, studio light")
        )
        "TRIANGULAR" -> listOf(
            PromptOpcion("Quiff estilizado", "textured quiff haircut, balanced top, fade sides"),
            PromptOpcion("Peinado con fleco", "fringe forward hairstyle, contemporary cut, soft edges")
        )
        "ALARGADO" -> listOf(
            PromptOpcion("Corte medio con flequillo", "medium haircut with bangs, soft shape, face framing"),
            PromptOpcion("Caesar cut", "caesar cut, short top with fringe, clean profile"),
            PromptOpcion("Fade medio natural", "mid fade, natural flow, realistic lighting")
        )
        else -> listOf(
            PromptOpcion("Estilo libre", "short stylish haircut, cinematic lighting, clean look")
        )
    }
}
