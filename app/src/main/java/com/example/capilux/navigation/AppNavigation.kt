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
import com.example.capilux.utils.EncryptedPrefs

@Composable
fun AppNavigation(
    darkModeState: MutableState<Boolean>,
    altThemeState: MutableState<Boolean>,
    usernameState: MutableState<String>
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = "splashDecision") {
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
        composable("main") {
            val username = usernameState.value
            val sharedPrefs = remember { EncryptedPrefs.getPrefs(context) }
            val imageUriString = sharedPrefs.getString("imageUri", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            MainScreen(
                navController = navController,
                username = username,
                profileImageUri = imageUri,
                useAltTheme = altThemeState.value
            )
        }
        composable("config") {
            val sharedPrefs = remember { EncryptedPrefs.getPrefs(context) }
            val savedImageUriString = sharedPrefs.getString("imageUri", null)
            val imageUri = savedImageUriString?.let { Uri.parse(it) }

            ConfigScreen(navController, usernameState, imageUri, darkModeState, altThemeState)
        }
        composable("confirmPhoto/{imageUri}") { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ConfirmPhotoScreen(uri, altThemeState.value, navController)
        }
        composable("processing/{imageUri}") { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ProcessingScreen(uri, altThemeState.value, navController)
        }
        composable(
            route = "analysisResult/{resultado}",
            arguments = listOf(navArgument("resultado") { type = NavType.StringType })
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getString("resultado") ?: ""
            val resultado = Uri.decode(raw)
            AnalysisResultScreen(resultado = resultado, navController = navController)
        }
        composable("filterPreview/{faceShape}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            FilterPreviewScreen(faceShape, navController)
        }
        composable("results/{faceShape}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            val recommendedStyles = getRecommendedStyles(faceShape)
            val imageUriString = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("last_captured_image", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            ResultsScreen(faceShape, recommendedStyles, imageUri, altThemeState.value)
        }
        composable("favorites") {
            FavoritesScreen()
        }
        composable("support") {
            SupportScreen(navController, altThemeState.value)
        }
        composable(
            route = "errorScreen/{message}",
            arguments = listOf(navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            val msg = Uri.decode(backStackEntry.arguments?.getString("message") ?: "Error desconocido")
            ErrorScreen(message = msg, useAltTheme = altThemeState.value, navController = navController)
        }
    }
}

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

fun getRecommendedFilters(faceShape: String): List<androidx.compose.ui.graphics.Color> {
    return listOf(
        androidx.compose.ui.graphics.Color.Transparent,
        androidx.compose.ui.graphics.Color(0x882575FC),
        androidx.compose.ui.graphics.Color(0x88FF8800)
    )
}
