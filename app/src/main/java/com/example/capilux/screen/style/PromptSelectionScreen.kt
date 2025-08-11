package com.example.capilux.screen.style

import androidx.compose.foundation.BorderStroke
import android.net.Uri as AndroidUri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.SharedViewModel
import com.example.capilux.components.LoadingOverlay
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.Normalizer

data class PromptOpcion(
    val nombreVisible: String,
    val promptTecnico: String
)

fun normalizeFaceShapeKey(raw: String?): String {
    if (raw.isNullOrBlank()) return "DESCONOCIDO"

    var s = Normalizer.normalize(raw, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
        .trim()
        .replace("[\\s-]+".toRegex(), "_")

    val map = mapOf(
        "ovalado" to "OVALADO",
        "cuadrado" to "CUADRADO",
        "redondo" to "REDONDO",
        "triangular" to "TRIANGULAR",
        "alargado" to "ALARGADO",
        "corazon" to "TRIANGULAR",
        "triangulo_invertido" to "TRIANGULAR",
        "diamante" to "TRIANGULAR",
        "rectangular" to "ALARGADO",
        "oblong" to "ALARGADO"
    )
    return map[s] ?: "DESCONOCIDO"
}

fun prettyFaceShapeLabel(key: String): String = when (key) {
    "OVALADO" -> "Ovalado"
    "CUADRADO" -> "Cuadrado"
    "REDONDO" -> "Redondo"
    "TRIANGULAR" -> "Triangular"
    "ALARGADO" -> "Alargado"
    else -> "Desconocido"
}

private const val RANDOM_COUNT: Int = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelectionScreen(
    faceShape: String,
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    useAltTheme: Boolean
) {
    val sourceShape = if (sharedViewModel.faceShape.isNotBlank()) sharedViewModel.faceShape else faceShape
    val key = remember(sourceShape) { normalizeFaceShapeKey(sourceShape) }
    val titleLabel = remember(key) { prettyFaceShapeLabel(key) }

    val allPrompts = remember(key) { getPrompts(key) }
    val prompts = remember(allPrompts) {
        if (RANDOM_COUNT > 0 && allPrompts.size > RANDOM_COUNT) {
            allPrompts.shuffled().take(RANDOM_COUNT)
        } else allPrompts
    }

    val loading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    val imageFile = File(context.filesDir, "original_usuario.jpg")
    val maskFile = File(context.filesDir, "mascara_tmp.png")

    val primaryColor = if (useAltTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val onPrimaryColor = if (useAltTheme) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.style_selection_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White // <- letras blancas
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceColor.copy(alpha = 0.3f))
                        .border(
                            width = 1.dp,
                            color = primaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (key == "DESCONOCIDO") {
                                stringResource(R.string.recommended_styles)
                            } else {
                                stringResource(R.string.recommended_styles_for, titleLabel.lowercase())
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White // <- letras blancas
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.select_style_prompt),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White // <- letras blancas
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Style Options
                prompts.forEach { opcion ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceColor.copy(alpha = 0.4f))
                            .border(
                                width = 1.dp,
                                color = primaryColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (!imageFile.exists() || !maskFile.exists()) {
                                    val error = AndroidUri.encode(context.getString(R.string.missing_image_mask))
                                    navController.navigate("errorScreen/$error")
                                    return@clickable
                                }
                                coroutineScope.launch {
                                    loading.value = true
                                    try {
                                        CapiluxApi.generarEstilo(
                                            context = context,
                                            imageUri = AndroidUri.fromFile(imageFile),
                                            mascaraFile = maskFile,
                                            prompt = opcion.promptTecnico,
                                            onSuccess = { resultado ->
                                                val resultFile = File(context.filesDir, "resultado_sd.png")
                                                resultFile.writeBytes(resultado)

                                                sharedViewModel.updateSelectedPrompt(opcion.nombreVisible)
                                                sharedViewModel.updateSelectedPromptText(opcion.promptTecnico)

                                                val encodedPath = AndroidUri.encode(resultFile.absolutePath)
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    navController.navigate("generatedImage/$encodedPath") {
                                                        popUpTo("main") { inclusive = false }
                                                    }
                                                }
                                            },
                                            onError = { mensaje ->
                                                val encodedMsg = AndroidUri.encode("${context.getString(R.string.error)}: $mensaje")
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    navController.navigate("errorScreen/$encodedMsg")
                                                }
                                            }
                                        )
                                    } catch (e: Exception) {
                                        val error = AndroidUri.encode("${context.getString(R.string.unexpected_error)}: ${e.message ?: ""}")
                                        coroutineScope.launch(Dispatchers.Main) {
                                            navController.navigate("errorScreen/$error")
                                        }
                                    } finally {
                                        loading.value = false
                                    }
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = opcion.nombreVisible,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White // <- letras blancas
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth(0.6f)
                                    .background(primaryColor.copy(alpha = 0.3f))
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.tap_to_preview),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White // <- letras blancas
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (loading.value) {
                LoadingOverlay(
                    message = stringResource(R.string.generating_style),
                    useAltTheme = useAltTheme
                )
            }
        }
    }
}
fun getPrompts(key: String): List<PromptOpcion> {
    return when (key) {
        "OVALADO" -> listOf(
            PromptOpcion("Corte con volumen arriba", "modern volumized haircut, clean sides, studio light"),
            PromptOpcion("Peinado lateral elegante", "elegant side part haircut, sharp look, natural lighting"),
            PromptOpcion("Fade alto con textura", "high textured fade, stylish look, hair texture focus"),
            PromptOpcion("Undercut con top largo", "undercut hairstyle, long textured top, matte finish, cinematic light"),
            PromptOpcion("Look casual despeinado", "messy casual haircut, medium length, soft lighting, natural vibe"),
            PromptOpcion("Slick back moderno", "slicked back medium hairstyle, glossy finish, fashion magazine shot"),
            PromptOpcion("Corte clásico con raya", "classic comb over haircut, clean fade, vintage touch"),
            PromptOpcion("Medium flow natural", "medium flow haircut, natural waves, daylight photo"),
            PromptOpcion("Ivy league", "ivy league haircut, side part, preppy style, sharp finish"),
            PromptOpcion("Textura suave y natural", "natural textured haircut, low fade, warm lighting")
        )
        "CUADRADO" -> listOf(
            PromptOpcion("Crew cut definido", "crew cut, masculine clean style, sharp sides"),
            PromptOpcion("Buzz cut militar", "buzz cut, military hairstyle, clean head, modern lighting"),
            PromptOpcion("Side part clásico", "classic side part, strong jaw emphasis, neat finish"),
            PromptOpcion("Fade bajo estructurado", "low fade, defined lines, modern style"),
            PromptOpcion("Volumen controlado", "controlled volume top, tapered sides, studio shot"),
            PromptOpcion("Texturizado alto", "high textured top, medium fade, natural daylight"),
            PromptOpcion("Peinado atrás pulido", "slicked back short hair, strong hold, studio background"),
            PromptOpcion("Retro corto", "retro short haircut, sharp sideburns, 60s vibe"),
            PromptOpcion("Militar moderno", "modern military haircut, skin fade, clean profile"),
            PromptOpcion("Corto mate texturizado", "matte textured short haircut, casual style")
        )
        "REDONDO" -> listOf(
            PromptOpcion("Pompadour moderno", "modern pompadour, stylish top volume, fade sides"),
            PromptOpcion("Flequillo hacia arriba", "fringe up hairstyle, edgy look, studio light"),
            PromptOpcion("Upper quiff texturizado", "textured upper quiff, added height, natural sides"),
            PromptOpcion("Mohawk suave", "soft mohawk, subtle fade, street style photo"),
            PromptOpcion("Spiky hair moderno", "short spiky hairstyle, clean fade, modern male look"),
            PromptOpcion("Faux hawk elegante", "faux hawk haircut, clean fade, professional look"),
            PromptOpcion("Fade con raya marcada", "fade haircut with hard part, defined lines"),
            PromptOpcion("Messy top casual", "messy top hairstyle, casual outfit, daylight"),
            PromptOpcion("Burst fade moderno", "burst fade haircut, textured top, urban fashion"),
            PromptOpcion("Medio con ondas", "medium wavy haircut, relaxed summer style")
        )
        "TRIANGULAR" -> listOf(
            PromptOpcion("Quiff estilizado", "textured quiff haircut, balanced top, fade sides"),
            PromptOpcion("Fleco suave", "soft fringe forward hairstyle, contemporary cut, soft edges"),
            PromptOpcion("Volumen superior controlado", "controlled top volume, tapered sides, balanced forehead-jaw ratio"),
            PromptOpcion("Layered haircut", "layered hairstyle, soft transitions, natural colors"),
            PromptOpcion("Semi largo con ondas", "medium wavy hairstyle, side sweep, cinematic background"),
            PromptOpcion("Slick back con volumen", "voluminous slick back hairstyle, medium length"),
            PromptOpcion("Brush up", "brush up hairstyle, soft fade, stylish look"),
            PromptOpcion("Medium pomp clásico", "classic medium pompadour, shiny finish"),
            PromptOpcion("Medio despeinado", "messy medium haircut, casual aesthetic"),
            PromptOpcion("Texturizado natural", "natural textured medium haircut, daylight")
        )
        "ALARGADO" -> listOf(
            PromptOpcion("Medio con flequillo", "medium haircut with bangs, soft shape, face framing"),
            PromptOpcion("Caesar cut", "caesar cut, short top with fringe, clean profile"),
            PromptOpcion("Fade medio natural", "mid fade, natural flow, realistic lighting"),
            PromptOpcion("Slick back clásico", "slicked back hairstyle, medium length, classy look"),
            PromptOpcion("Bro flow", "bro flow haircut, natural waves, relaxed style"),
            PromptOpcion("Side part suave", "soft side part haircut, low fade, neat lines"),
            PromptOpcion("Top largo texturizado", "long textured top, short sides, casual vibe"),
            PromptOpcion("Largo hacia atrás", "long slick back haircut, glossy finish"),
            PromptOpcion("Medio despeinado", "messy medium length hairstyle, wind-blown look"),
            PromptOpcion("Escalonado medio", "layered medium haircut, soft layers, natural light")
        )
        else -> listOf(
            PromptOpcion("Estilo libre", "short stylish haircut, cinematic lighting, clean look"),
            PromptOpcion("Corte creativo artístico", "avant-garde haircut, experimental shapes, high fashion photo"),
            PromptOpcion("Look vintage retro", "retro hairstyle, 80s inspired, soft film grain effect"),
            PromptOpcion("Urbano moderno", "modern urban haircut, street fashion photo"),
            PromptOpcion("Casual juvenil", "casual young men haircut, summer vibe, daylight")
        )
    }
}
