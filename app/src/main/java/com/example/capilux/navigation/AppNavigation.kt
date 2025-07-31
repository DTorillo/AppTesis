package com.example.capilux.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.capilux.screen.*
import com.example.capilux.SharedViewModel
import com.example.capilux.utils.EncryptedPrefs

@Composable
fun AppNavigation(
    darkModeState: MutableState<Boolean>,
    altThemeState: MutableState<Boolean>,
    usernameState: MutableState<String>,
    sharedViewModel: SharedViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = "splashDecision") {
        // ----------- FLUJO INICIAL -----------
        composable("splashDecision") {
            SplashDecisionScreen(navController, altThemeState.value)
        }
        composable("welcome") {
            WelcomeScreen(navController, altThemeState.value)
        }
        composable("userCreation") {
            UserCreationScreen(navController, altThemeState.value, usernameState)
        }
        composable("setupSecurity") {
            SetupSecurityScreen(navController, altThemeState.value)
        }
        composable("auth") {
            AuthScreen(navController, altThemeState.value)
        }
        composable("resetPin") {
            ResetPinScreen(navController, altThemeState.value)
        }

        // ----------- MAIN Y CONFIGURACIÓN -----------
        composable("main") {
            val username = usernameState.value
            val sharedPrefs = remember { EncryptedPrefs.getPrefs(context) }
            val imageUriString = sharedPrefs.getString("imageUri", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            MainScreen(
                navController = navController,
                username = username,
                profileImageUri = imageUri,
                useAltTheme = altThemeState.value,
                sharedViewModel = sharedViewModel
            )
        }
        composable("config") {
            val sharedPrefs = remember { EncryptedPrefs.getPrefs(context) }
            val savedImageUriString = sharedPrefs.getString("imageUri", null)
            val imageUri = savedImageUriString?.let { Uri.parse(it) }

            ConfigScreen(navController, usernameState, imageUri, darkModeState, altThemeState)
        }

        // ----------- FLUJO IA - PROCESO MODULAR -----------
        // Confirmación de foto (siempre pasa path absoluto o URI válido)
        composable("confirmPhoto/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ConfirmPhotoScreen(
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        // Procesamiento de análisis facial
        composable("processing/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ProcessingScreen(
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController
            )
        }

        // Resultados del análisis facial (solo muestra resultado, no llama IA)
        composable(
            route = "analysisResult/{resultado}/{imageUri}",
            arguments = listOf(
                navArgument("resultado") { type = NavType.StringType },
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("resultado") ?: ""
            val resultado = Uri.decode(raw)
            val encodedImage = backStackEntry.arguments?.getString("imageUri") ?: ""
            val imageUri = Uri.decode(encodedImage)
            AnalysisResultScreen(
                resultado = resultado,
                imageUri = imageUri,
                navController = navController,
                useAltTheme = altThemeState.value
            )
        }

        // Generación de máscara (solo IA de máscara, NO generativa)
        composable("maskProcessingScreen/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            MaskProcessingScreen(
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController
            )
        }

        // Previsualización de máscara (foto original y máscara lado a lado)
        composable("maskPreviewScreen/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            MaskPreviewScreen(
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController
            )
        }

        // Selección de corte/estilo (prompt). Pasa el path de la imagen original.
        composable("promptSelectionScreen/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            PromptSelectionScreen(
                faceShape = sharedViewModel.faceShape, // O pásalo por argumento si prefieres
                navController = navController,
                sharedViewModel = sharedViewModel,
                useAltTheme = altThemeState.value
            )
        }

        // Imagen generada por la IA (resultado final)
        composable("generatedImage/{imagePath}") { backStackEntry ->
            val imagePath = Uri.decode(backStackEntry.arguments?.getString("imagePath") ?: "")
            GeneratedImageScreen(
                navController = navController,
                imageUri = imagePath, // <--- El parámetro aquí es imageUri
                sharedViewModel = sharedViewModel,
                useAltTheme = altThemeState.value
            )
        }



        // Pantalla de error
        composable(
            route = "errorScreen/{message}",
            arguments = listOf(navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            val msg = Uri.decode(backStackEntry.arguments?.getString("message") ?: "Error desconocido")
            ErrorScreen(
                message = msg,
                useAltTheme = altThemeState.value,
                navController = navController
            )
        }

        // ----------- OTRAS PANTALLAS -----------
        composable("results/{faceShape}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            val recommendedStyles = getRecommendedStyles(faceShape)
            val imageUriString = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("last_captured_image", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            ResultsScreen(
                faceShape = faceShape,
                recommendedStyles = recommendedStyles,
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable("savedImages") {
            SavedImagesScreen(navController, altThemeState.value)
        }
        composable("support") {
            SupportScreen(navController, altThemeState.value)
        }
    }
}

// ---- Utilidad para recomendaciones ----
fun getRecommendedStyles(faceShape: String): List<String> {
    return when (faceShape.lowercase()) {
        "ovalado"     -> listOf("Pompadour", "Undercut", "Corte clásico")
        "redondo"     -> listOf("Volumen arriba", "Raya al lado", "Corte angular")
        "cuadrado"    -> listOf("Fade", "Buzz cut", "Peinado hacia atrás")
        "alargado"    -> listOf("Flequillo", "Laterales con volumen", "Corte balanceado")
        "triangular"  -> listOf("Volumen superior", "Texturizado", "Peinado con caída")
        "corazon"     -> listOf("Desconectado", "Peinado ligero", "Fade con barba")
        "diamante"    -> listOf("Side part", "Corte alto con forma", "Textura arriba")
        "rectangular" -> listOf("Pompadour", "Militar", "Despeinado superior")
        "trapecio"    -> listOf("Fade alto", "Peinado balanceado", "Contornos suaves")
        "perla"       -> listOf("Corte creativo", "Estilo personalizado", "Diseño libre")
        else          -> listOf("Corte moderno", "Corte clásico", "Estilo versátil")
    }
}
