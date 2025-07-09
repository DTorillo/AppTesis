package com.example.capilux.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capilux.screen.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

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
        composable("explanation") {
            ExplanationScreen(navController, altThemeState.value)
        }
        composable("userCreation") {
            UserCreationScreen(navController, altThemeState.value, usernameState)
        }
        composable("setupSecurity") {
            BiometricSetupScreen(navController, altThemeState.value)
        }
        composable("login") {
            LoginScreen(navController, altThemeState.value)
        }
        composable("main") {
            val username = usernameState.value
            val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
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
            val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
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
        composable("analysisResult/{faceShape}/{ratio}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            val ratio = backStackEntry.arguments?.getString("ratio")?.toFloatOrNull() ?: 1f
            AnalysisResultScreen(faceShape, ratio, navController)
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

            ResultsScreen(faceShape, recommendedStyles, imageUri)
        }
        composable("favorites") {
            FavoritesScreen()
        }
    }
}

// Utilidades
fun getRecommendedStyles(faceShape: String): List<String> {
    return when (faceShape.lowercase()) {
        "ovalada" -> listOf("Corte clásico", "Peinado hacia atrás", "Corte degradado")
        "redonda" -> listOf("Corte con volumen arriba", "Undercut", "Peinado con raya al lado")
        "cuadrada" -> listOf("Corte buzz", "Fade", "Corte texturizado")
        "alargada" -> listOf("Flequillo", "Corte con volumen a los lados", "Ondas naturales")
        else -> listOf("Corte clásico", "Corte moderno", "Estilo versátil")
    }
}

fun getRecommendedFilters(faceShape: String): List<Color> {
    return listOf(Color.Transparent, Color(0x8800FF00), Color(0x88FF8800))
}
