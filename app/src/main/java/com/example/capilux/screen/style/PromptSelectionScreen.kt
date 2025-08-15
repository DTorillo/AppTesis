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
            PromptOpcion(
                "Corte con volumen arriba",
                "studio portrait, head-and-shoulders, oval face, modern volumized haircut on top, soft taper on sides, light texture, natural hairline, neat neckline, realistic hair strands, 85mm photo, softbox key light + subtle rim light, neutral background, no hat"
            ),
            PromptOpcion(
                "Peinado lateral elegante",
                "studio portrait, oval face, elegant deep side part, medium length on top, low taper on sides, combed and polished finish, controlled flyaways, refined silhouette, magazine quality, soft natural lighting, no headwear"
            ),
            PromptOpcion(
                "Fade alto con textura",
                "clean studio photo, oval face, high fade, textured crop on top, messy but controlled texture, matte finish, barbershop detail, sharp edges around ears, high detail hair strands, soft directional light"
            ),
            PromptOpcion(
                "Undercut con top largo",
                "studio portrait, oval face, disconnected undercut, long textured top swept back, airy volume, matte clay finish, sides short and clean, cinematic key light, subtle rim light, realistic detail, no hat"
            ),
            PromptOpcion(
                "Look casual despeinado",
                "daylight portrait, oval face, messy casual hairstyle, medium length, natural movement, loose waves, soft volume, light frizz for realism, soft diffusion light, lifestyle vibe"
            ),
            PromptOpcion(
                "Slick back moderno",
                "studio portrait, oval face, medium length slicked back hairstyle, slightly elevated front, glossy pomade finish, clean sides, fashion editorial look, specular highlights on hair, gradient background"
            ),
            PromptOpcion(
                "Corte clásico con raya",
                "vintage-inspired studio portrait, oval face, classic side part, mid fade, scissor cut on top, tidy comb lines, barbershop clean finish, natural shine, warm key light, no hat"
            ),
            PromptOpcion(
                "Medium flow natural",
                "natural light portrait, oval face, medium flow haircut, layered with soft movement, center or loose side part, lightweight texture, healthy natural sheen, shallow depth of field, realistic hair strands"
            ),
            PromptOpcion(
                "Ivy league",
                "studio portrait, oval face, ivy league haircut, short sides with taper, longer top with refined side part, neat and preppy finish, subtle texture, professional lighting, neutral background"
            ),
            PromptOpcion(
                "Textura suave y natural",
                "soft studio portrait, oval face, low fade with natural textured top, feathered layers, soft edges around hairline, warm lighting, realistic strand detail, minimal flyaways"
            ),
            PromptOpcion(
                "Capas largas con contorno",
                "portrait, oval face, long layered haircut, face-framing layers, soft feathered ends, natural shine, subtle curtain flow, beauty lighting, high fidelity hair detail, no headwear"
            ),
            PromptOpcion(
                "Lob recto (long bob) pulido",
                "studio portrait, oval face, sleek long bob (lob), blunt ends, neck to shoulder length, center part, glossy straight finish, clean outline, high detail strands, softbox lighting, no hat"
            ),
            PromptOpcion(
                "Shag moderno con flequillo",
                "editorial portrait, oval face, modern shag haircut, wispy bangs, layered texture, tousled natural movement, matte finish, soft rim light to separate hair, neutral backdrop"
            ),
            PromptOpcion(
                "Wolf cut suave",
                "studio portrait, oval face, soft wolf cut, long layers with shaggy texture, light curtain bangs, airy volume, realistic strand separation, cinematic lighting, no headwear"
            ),
            PromptOpcion(
                "Rizos medianos definidos",
                "daylight portrait, oval face, medium curls, defined coils, hydrated look, controlled frizz, side part option, curl definition emphasis, macro hair detail, soft natural light"
            ),
            PromptOpcion(
                "Afro corto shape-up",
                "studio portrait, oval face, short afro with clean shape-up, crisp hairline, defined tight curls, moisturized texture, even lighting, no hat, neutral background"
            ),
            PromptOpcion(
                "Mullet moderno",
                "studio portrait, oval face, modern mullet, textured top, slightly longer back, soft taper on sides, matte finish, trendy editorial vibe, rim light to outline hair, high detail"
            ),
            PromptOpcion(
                "Quiff texturizado",
                "studio portrait, oval face, textured quiff with lifted front, mid fade, matte clay finish, visible strand separation, controlled side volume, soft directional key light"
            ),
            PromptOpcion(
                "Pompadour clásico",
                "studio portrait, oval face, classic pompadour, high but smooth front volume, clean fade on sides, high-shine finish, barbershop precision, dramatic key light, neutral background"
            ),
            PromptOpcion(
                "Pixie largo peinado lateral",
                "studio portrait, oval face, long pixie cut, textured top with side-swept fringe, soft feminine/androgynous vibe, natural shine, subtle rim light, realistic strand detail"
            )
        )

        "CUADRADO" -> listOf(
            PromptOpcion(
                "Crew cut definido",
                "studio portrait, head-and-shoulders, square face, strong jawline, classic crew cut, short cropped top with slight forward texture, tight taper on sides, clean neckline, barbershop precision, realistic hair strand detail, 85mm photo, softbox key light + subtle rim light, neutral background, no hat"
            ),
            PromptOpcion(
                "Buzz cut militar",
                "clean studio photo, square face, military buzz cut (very short uniform length), optional skin fade at temples, crisp outline around ears, matte finish, high micro-detail on hair and scalp shading, dramatic single key light with soft fill, neutral gray background, no headwear"
            ),
            PromptOpcion(
                "Side part clásico",
                "studio portrait, square face, classic side part, longer top (4–6 cm) to slightly elongate, low or mid taper on sides to reduce width, visible comb lines, polished barbershop finish, subtle natural shine, soft directional lighting, 85mm lens, no hat"
            ),
            PromptOpcion(
                "Fade bajo estructurado",
                "studio portrait, square face, low fade with structured shape, tight outline at ears and nape, clean temple corners, short textured top, matte clay finish, crisp line work, soft rim light to define jaw contour, neutral backdrop, no accessories"
            ),
            PromptOpcion(
                "Volumen controlado",
                "editorial studio portrait, square face, controlled volume on top with lifted front, tapered sides kept slim, lightweight natural texture, neat hairline, semi-matte finish, beauty panel key light + gentle fill, shallow depth of field, magazine quality"
            ),
            PromptOpcion(
                "Texturizado alto",
                "daylight portrait, square face, high textured top with separated strands, medium fade sides kept narrow, matte finish, dynamic yet tidy look, lifestyle vibe, soft natural light, shallow depth of field, no hats"
            ),
            PromptOpcion(
                "Peinado atrás pulido",
                "studio portrait, square face, short slicked-back hairstyle, slight front height to elongate silhouette, tight sides (low fade or taper), high-shine pomade, clean combed-back channels, fashion editorial gradient background, specular highlights on hair, no headwear"
            ),
            PromptOpcion(
                "Retro corto",
                "vintage-inspired studio portrait, square face, 60s short haircut, tidy sideburns, clean taper, subtle side part, natural non-greasy shine, warm key light with gentle fill, tasteful film grain, neutral backdrop"
            ),
            PromptOpcion(
                "Militar moderno",
                "studio portrait, square face, modern military haircut (high and tight), skin fade on sides, short cropped top (1–1.5 cm), sharp and disciplined outline, defined temple corners, dramatic hard key + soft fill, neutral gray background, no hat"
            ),
            PromptOpcion(
                "Corto mate texturizado",
                "studio portrait, square face, short matte textured haircut, choppy layers on top, low-to-mid taper on sides to keep width slim, natural hairline, controlled flyaways, high-fidelity hair strand detail, soft key light, neutral studio background"
            )
        )

        "REDONDO" -> listOf(
            PromptOpcion(
                "Pompadour moderno",
                "studio portrait, head-and-shoulders, round face, modern pompadour with strong vertical lift, semi-high crown, tight low fade on sides to minimize width, clean hairline, controlled flyaways, realistic hair strands, 85mm photo, softbox key light + subtle rim light, neutral background, no hat"
            ),
            PromptOpcion(
                "Flequillo hacia arriba",
                "clean studio photo, round face, fringe up hairstyle (up-swept bangs), medium length top with matte finish, low taper on sides to slim the silhouette, defined but soft edges, natural highlights, soft directional lighting, no headwear"
            ),
            PromptOpcion(
                "Upper quiff texturizado",
                "studio portrait, round face, textured upper quiff with lifted front, layered separation for height, mid taper or low fade sides (narrow profile), matte clay finish, barbershop precision, soft rim light to outline hair, neutral backdrop"
            ),
            PromptOpcion(
                "Mohawk suave",
                "daylight editorial portrait, round face, soft mohawk with subtle center ridge, faded sides kept slim, medium length on top, controlled texture, natural matte finish, street style vibe, shallow depth of field, no hat"
            ),
            PromptOpcion(
                "Spiky hair moderno",
                "studio portrait, round face, short spiky hairstyle with vertical emphasis, clean mid fade, spikes separated and tidy, matte wax finish, crisp outline around ears, balanced key light with gentle fill, neutral background"
            ),
            PromptOpcion(
                "Faux hawk elegante",
                "studio portrait, round face, elegant faux hawk, tapered or skin fade sides to reduce width, medium top with centered height, refined silhouette, subtle shine, professional magazine look, rim light for contour, no headwear"
            ),
            PromptOpcion(
                "Fade con raya marcada",
                "studio portrait, round face, hard part with fade, defined razor line, longer top combed over to add height, low-to-mid fade sides kept narrow, polished finish, realistic strand detail, neutral studio lighting, no hat"
            ),
            PromptOpcion(
                "Messy top casual",
                "lifestyle daylight portrait, round face, messy textured top with extra crown height, low taper sides to slim the face, natural movement, soft diffusion light, casual vibe, shallow depth of field"
            ),
            PromptOpcion(
                "Burst fade moderno",
                "urban fashion portrait, round face, burst fade around the ear, textured top with vertical lift, clean temple edges, matte finish, editorial lighting with subtle rim, high fidelity hair detail, no accessories"
            ),
            PromptOpcion(
                "Medio con ondas",
                "daylight portrait, round face, medium wavy haircut with lift at the crown, loose natural waves, sides tucked and neat to avoid width, hydrated sheen (not glossy), soft natural light, lifestyle summer feel, no headwear"
            )
        )

        "TRIANGULAR" -> listOf(
            PromptOpcion(
                "Quiff estilizado",
                "studio portrait, head-and-shoulders, triangular face (wider jaw, narrower forehead), textured quiff with lifted front, slight volume at temples to broaden upper face, mid taper sides (not skin fade), matte finish, precise hairline, realistic hair strands, 85mm photo, softbox key light + subtle rim light, neutral background, no hat"
            ),
            PromptOpcion(
                "Fleco suave",
                "clean studio photo, triangular face, soft forward fringe lightly side-swept, layered texture around temples to add width, gentle taper on sides, soft edges, natural matte finish, controlled flyaways, soft directional lighting, no headwear"
            ),
            PromptOpcion(
                "Volumen superior controlado",
                "studio portrait, triangular face, controlled top volume with slight crown lift, light fullness at temples for balance, tapered sides kept moderate (avoid ultra tight), balanced forehead–jaw ratio emphasis, refined silhouette, neutral studio lighting"
            ),
            PromptOpcion(
                "Layered haircut",
                "editorial portrait, triangular face, layered hairstyle with soft transitions, temple build-up to broaden upper third, subtle side sweep, natural color and sheen, tidy nape, beauty lighting with gentle fill, high strand fidelity, no hat"
            ),
            PromptOpcion(
                "Semi largo con ondas",
                "cinematic portrait, triangular face, medium wavy hairstyle, side sweep across forehead, soft volume near temples, tidy sides to avoid extra jaw width, airy movement, realistic strand separation, cinematic background, soft rim light"
            ),
            PromptOpcion(
                "Slick back con volumen",
                "studio portrait, triangular face, voluminous slick back (medium length), raised front and gentle temple fullness, low taper sides (not too tight), glossy pomade finish, clean outline around ears, fashion editorial gradient background, specular highlights on hair"
            ),
            PromptOpcion(
                "Brush up",
                "studio portrait, triangular face, brush up hairstyle with vertical lift, soft fade or low taper sides to keep upper width balanced, matte clay finish, controlled texture, crisp but natural hairline, soft key light + subtle rim, neutral backdrop"
            ),
            PromptOpcion(
                "Medium pomp clásico",
                "studio portrait, triangular face, classic medium pompadour, smooth front height, gentle side volume near temples, low-to-mid taper for balance, polished shine, barbershop precision, 85mm lens look, softbox lighting, no headwear"
            ),
            PromptOpcion(
                "Medio despeinado",
                "daylight portrait, triangular face, messy medium haircut with side-swept movement, layered texture adding width at temples, tidy sides and nape to avoid jaw emphasis, natural matte finish, lifestyle vibe, shallow depth of field"
            ),
            PromptOpcion(
                "Texturizado natural",
                "natural light portrait, triangular face, medium length with natural textured layers, soft temple build-out, low taper sides, realistic strand detail, warm daylight, neutral background, no hat"
            )
        )

        "ALARGADO" -> listOf(
            PromptOpcion(
                "Medio con flequillo",
                "studio portrait, head-and-shoulders, long/oblong face, medium haircut with soft bangs across the forehead, gentle curtain fringe framing the face, slight fullness at the temples to add width, low taper on sides, matte natural finish, realistic hair strand detail, 85mm photo, softbox key light + subtle rim light, neutral background, no headwear"
            ),
            PromptOpcion(
                "Caesar cut",
                "clean studio photo, oblong face, caesar cut with short textured top brushed forward, neat straight fringe, low fade or tight taper to keep sides tidy (not ultra high), crisp outline around ears and nape, matte finish, barbershop precision, soft directional lighting"
            ),
            PromptOpcion(
                "Fade medio natural",
                "studio portrait, oblong face, mid fade with natural flow on top, controlled crown height, hair styled slightly forward for balance, subtle temple build-out to reduce verticality, semi-matte finish, neutral studio lighting, shallow depth of field"
            ),
            PromptOpcion(
                "Slick back clásico",
                "editorial studio portrait, oblong face, classic slicked back hairstyle, medium length with low-profile front lift, gentle side fullness for balance, clean part lines, glossy pomade finish, fashion magazine look, gradient background, specular highlights on hair"
            ),
            PromptOpcion(
                "Bro flow",
                "daylight portrait, oblong face, medium-length bro flow, natural waves flowing back with slight side sweep, relaxed movement, soft volume near temples, tidy sides (low taper), healthy natural sheen, lifestyle vibe, soft natural light, no hat"
            ),
            PromptOpcion(
                "Side part suave",
                "studio portrait, oblong face, soft side part with neat comb lines, low fade to maintain side presence, medium-short top with modest lift, balanced forehead–jaw proportions, semi-matte finish, subtle rim light, neutral background"
            ),
            PromptOpcion(
                "Top largo texturizado",
                "clean studio photo, oblong face, long textured top with forward-leaning separation, low taper sides (not too tight) to avoid extra length, matte clay finish, visible strand separation, controlled crown height, soft key light + gentle fill"
            ),
            PromptOpcion(
                "Largo hacia atrás",
                "studio portrait, oblong face, long slicked-back haircut, smooth laid-back profile with modest front elevation, side fullness for horizontal balance, glossy refined finish, crisp hairline, fashion editorial background, high-fidelity hair detail"
            ),
            PromptOpcion(
                "Medio despeinado",
                "daylight lifestyle portrait, oblong face, messy medium-length hairstyle with wind-blown horizontal movement, controlled crown height, light texture, soft taper on sides, natural matte finish, shallow depth of field"
            ),
            PromptOpcion(
                "Escalonado medio",
                "natural light portrait, oblong face, layered medium haircut with soft face-framing layers, added width at temples, gentle side sweep, tidy nape, realistic strand detail, warm daylight, neutral backdrop, no headwear"
            )
        )

        else -> listOf(
            PromptOpcion(
                "Estilo libre",
                "studio portrait, head-and-shoulders, versatile short stylish haircut, subtle top texture, clean structured sides, neat neckline, realistic hair strands, 85mm photo look, softbox key light + subtle rim light, neutral background, no hat"
            ),
            PromptOpcion(
                "Corte creativo artístico",
                "high-fashion editorial portrait, avant-garde haircut with experimental shapes and sculpted silhouette, asymmetry allowed, bold texture, glossy or wet finish, dramatic lighting (hard key with colored gel rim), ultra-detailed hair rendering, runway vibe, gradient studio background, no accessories"
            ),
            PromptOpcion(
                "Look vintage retro",
                "retro 80s-inspired portrait, medium-length hairstyle with feathered layers and soft side part, gentle volume, natural shine, warm tungsten key light, soft film grain and slight halation, pastel studio backdrop, head-and-shoulders, no hat"
            ),
            PromptOpcion(
                "Urbano moderno",
                "street fashion portrait, modern urban haircut, mid or skin fade with textured top, matte finish, crisp outline around ears, shallow depth of field, city background bokeh, directional sunlight or neon accent rim light, realistic strand separation, no headwear"
            ),
            PromptOpcion(
                "Casual juvenil",
                "daylight lifestyle portrait, casual young men’s haircut, medium-short with light natural waves, relaxed movement, low taper sides, soft diffusion light, summer vibe, outdoor bokeh background, warm color grading, no hat"
            )
        )

    }
}
