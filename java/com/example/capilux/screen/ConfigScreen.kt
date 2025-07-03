package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.components.ProfileImageLarge
import com.example.capilux.components.SettingItem
import com.example.capilux.ui.theme.BaseDialog
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.SecondaryButton
import com.example.capilux.utils.restartApp
import com.example.capilux.utils.setAppLocale
import com.example.capilux.utils.compressImage

@Composable
fun ConfigScreen(
    navController: NavHostController,
    username: String,
    imageUri: Uri?,
    darkModeState: MutableState<Boolean> // Recibimos el estado del tema
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    var currentImageUri by remember { mutableStateOf(imageUri) }

    // Estado para las configuraciones
    var notificationsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("notifications_enabled", true))
    }
    var darkModeEnabled by remember { mutableStateOf(darkModeState.value) } // Usamos el estado pasado
    var currentLanguage by remember {
        mutableStateOf(sharedPreferences.getString("language", "es") ?: "es")
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val compressedUri = compressImage(context, it)
            currentImageUri = compressedUri // Actualizar estado local
            sharedPrefs.edit().putString("imageUri", compressedUri.toString()).apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
        ) {
            ProfileImageLarge(imageUri = currentImageUri)
        }
        // Nombre de usuario
        Text(
            text = username,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Configuraciones
        Text(
            text = "Configuraciones",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opción de Notificaciones
        SettingItem(title = "Notificaciones") {
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isEnabled ->
                    notificationsEnabled = isEnabled
                    sharedPreferences.edit().putBoolean("notifications_enabled", isEnabled).apply()
                }
            )
        }

        // Opción de Modo Oscuro
        SettingItem(title = "Modo Oscuro") {
            Switch(
                checked = darkModeEnabled,
                onCheckedChange = {
                    darkModeEnabled = it
                    darkModeState.value = it
                    sharedPreferences.edit().putBoolean("dark_mode_enabled", it).apply()
                }
            )
        }

        // Opción de Idioma
        var showLanguageDialog by remember { mutableStateOf(false) }
        SettingItem(
            title = "Idioma",
            modifier = Modifier.clickable { showLanguageDialog = true }
        ) {
            Text(
                text = when (currentLanguage) {
                    "es" -> "Español"
                    "en" -> "English"
                    else -> "Español"
                },
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Guardar cambios",
            onClick = {
                // ... (guardar configuraciones) ...
                navController.popBackStack()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            text = "Volver",
            onClick = { navController.popBackStack() }
        )

        // Diálogo de idioma
        if (showLanguageDialog) {
            BaseDialog(
                title = "Seleccionar idioma",
                onDismiss = { showLanguageDialog = false },
                content = {
                    // ... (contenido del selector de idioma) ...
                },
                confirmButton = {
                    SecondaryButton(
                        text = "Cerrar",
                        onClick = { showLanguageDialog = false }
                    )
                }
            )
        }
    }
}