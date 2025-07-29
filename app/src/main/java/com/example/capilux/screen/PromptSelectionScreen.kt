package com.example.capilux.screen

import android.content.Context
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
import com.example.capilux.network.GenerationApi
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.launch
import java.io.File

// Data class para vincular el texto visible con el prompt real
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
                    val uri = sharedViewModel.imageUri
                    if (uri != null) {
                        coroutineScope.launch {
                            loading.value = true
                            try {
                                val file = uriToTempFile(uri, context)
                                val promptFinal = opcion.promptTecnico
                                val resultado = GenerationApi.enviarImagenConPrompt(file, promptFinal)

                                if (resultado != null) {
                                    File(context.filesDir, "resultado_sd.png").writeBytes(resultado)
                                    sharedViewModel.updateSelectedPrompt(opcion.nombreVisible)
                                    navController.navigate("generated_image")
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                loading.value = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(opcion.nombreVisible)
            }
        }

        if (loading.value) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

fun uriToTempFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "foto.jpg")
    inputStream?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
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
