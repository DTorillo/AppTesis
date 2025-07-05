package com.example.capilux.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capilux.screen.ConfigScreen
import com.example.capilux.screen.MainScreen
import com.example.capilux.screen.ResultsScreen
import com.example.capilux.screen.ExplanationScreen
import com.example.capilux.screen.FavoritesScreen
import com.example.capilux.screen.UserCreationScreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AppNavigation(
    darkModeState: MutableState<Boolean>,
    altThemeState: MutableState<Boolean>,
    startDestination: String = "explanation" // Valor por defecto
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController, startDestination = startDestination) {
        composable("explanation") {
            ExplanationScreen(navController, altThemeState.value)
        }
        composable("userCreation") {
            UserCreationScreen(navController, altThemeState.value)
        }
        composable("main/{username}") { backStackEntry ->
            // 1. Obtener el nombre de usuario de los argumentos de navegación
            val username = backStackEntry.arguments?.getString("username") ?: ""

            // 2. Obtener la URI de la imagen desde SharedPreferences
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
            val imageUriString = sharedPrefs.getString("imageUri", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            // 3. Pasar ambos a MainScreen
            MainScreen(
                navController = navController,
                username = username,
                profileImageUri = imageUri,
                useAltTheme = altThemeState.value
            )
        }
        composable("config") {
            val context = LocalContext.current
            val sharedPreferences = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
            val savedImageUriString = sharedPreferences.getString("imageUri", null)
            val imageUri = savedImageUriString?.let { Uri.parse(it) }
            val savedUsername = sharedPreferences.getString("username", "") ?: ""

            // Pasamos darkModeState y altThemeState a ConfigScreen
            ConfigScreen(navController, savedUsername, imageUri, darkModeState, altThemeState)
        }
        composable("results/{faceShape}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            val recommendedStyles = getRecommendedStyles(faceShape)
            val context = LocalContext.current
            val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val imageUriString = sharedPrefs.getString("last_captured_image", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }

            ResultsScreen(faceShape, recommendedStyles, imageUri)
        }
        composable("favorites") {
            // Nueva pantalla de Estilos Favoritos
            FavoritesScreen()
        }
    }
}

// Funciones de utilidad
private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
        File(it, "Capilux").apply { mkdirs() }
    }
    return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}

private fun createFile(baseFolder: File, prefix: String, extension: String): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(System.currentTimeMillis())
    return File(baseFolder, "${prefix}${timestamp}${extension}")
}

fun getRecommendedStyles(faceShape: String): List<String> {
    return when (faceShape.lowercase()) {
        "ovalada" -> listOf("Corte clásico", "Peinado hacia atrás", "Corte degradado")
        "redonda" -> listOf("Corte con volumen arriba", "Undercut", "Peinado con raya al lado")
        "cuadrada" -> listOf("Corte buzz", "Fade", "Corte texturizado")
        "alargada" -> listOf("Flequillo", "Corte con volumen a los lados", "Ondas naturales")
        else -> listOf("Corte clásico", "Corte moderno", "Estilo versátil")
    }
}
