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
import com.example.capilux.SharedViewModel
import com.example.capilux.screen.*
import com.example.capilux.screen.analysis.AnalysisResultScreen
import com.example.capilux.screen.analysis.ProcessingScreen
import com.example.capilux.screen.mask.MaskPreviewScreen
import com.example.capilux.screen.mask.MaskProcessingScreen
import com.example.capilux.screen.style.GeneratedImageScreen
import com.example.capilux.screen.style.PromptSelectionScreen
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
        composable("confirmPhoto/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ConfirmPhotoScreen(imageUri, altThemeState.value, navController, sharedViewModel)
        }
        composable("processing/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            ProcessingScreen(imageUri, altThemeState.value, navController, sharedViewModel)
        }
        composable("analysisResult") {
            val resultado = sharedViewModel.analysisResult
            val imageUri = sharedViewModel.imageUri?.toString() ?: ""
            AnalysisResultScreen(resultado, imageUri, navController, altThemeState.value)
        }
        composable("maskProcessingScreen/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            MaskProcessingScreen(imageUri, altThemeState.value, navController)
        }
        composable("maskPreviewScreen/{imageUri}") { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            MaskPreviewScreen(imageUri, altThemeState.value, navController)
        }
        composable("promptSelectionScreen/{imageUri}") { backStackEntry ->
            PromptSelectionScreen(
                faceShape = sharedViewModel.faceShape,
                navController = navController,
                sharedViewModel = sharedViewModel,
                useAltTheme = altThemeState.value
            )
        }

        // RUTA CORREGIDA: usar siempre imageUri como parámetro
        composable(
            route = "generatedImage/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            GeneratedImageScreen(
                navController = navController,
                imageUri = imageUri,
                sharedViewModel = sharedViewModel,
                useAltTheme = altThemeState.value
            )
        }

        composable("errorScreen/{message}") { backStackEntry ->
            val msg = Uri.decode(backStackEntry.arguments?.getString("message") ?: "Error desconocido")
            ErrorScreen(msg, altThemeState.value, navController)
        }
        composable("results/{faceShape}") { backStackEntry ->
            val faceShape = backStackEntry.arguments?.getString("faceShape") ?: ""
            val imageUriString = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("last_captured_image", null)
            val imageUri = imageUriString?.let { Uri.parse(it) }
            ResultsScreen(
                faceShape = faceShape,
                recommendedStyles = emptyList(),
                imageUri = imageUri,
                useAltTheme = altThemeState.value,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }

        composable("savedImages") { SavedImagesScreen(navController, altThemeState.value) }
        composable("support") { SupportScreen(navController, altThemeState.value) }
    }
}
