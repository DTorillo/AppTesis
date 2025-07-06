package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.capilux.components.ProfileImageLarge
import com.example.capilux.ui.theme.BaseDialog
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.SecondaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.restartApp
import com.example.capilux.utils.setAppLocale
import com.example.capilux.utils.compressImage

@Composable
fun ConfigScreen(
    navController: NavHostController,
    usernameState: MutableState<String>,
    imageUri: Uri?,
    darkModeState: MutableState<Boolean>, // Recibimos el estado del tema
    altThemeState: MutableState<Boolean>
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    var currentImageUri by remember { mutableStateOf(imageUri) }

    // Estado para las configuraciones
    var altThemeEnabled by remember { mutableStateOf(altThemeState.value) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient(altThemeEnabled))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Box(
            modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
        ) {
            ProfileImageLarge(imageUri = currentImageUri)
        }
        // Nombre de usuario
        var editedUsername by remember { mutableStateOf(usernameState.value) }
        OutlinedTextField(
            value = editedUsername,
            onValueChange = { editedUsername = it },
            label = { Text("Nombre", color = Color.White) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Configuraciones
        Text(
            text = "Configuraciones",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones de tema

        // Opción de Tema Alternativo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Degradado",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = altThemeEnabled,
                onCheckedChange = {
                    altThemeEnabled = it
                    altThemeState.value = it
                    sharedPreferences.edit()
                        .putBoolean("alt_theme_enabled", it).apply()
                },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    uncheckedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.5f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }

        // Opción de Idioma
        var showLanguageDialog by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { showLanguageDialog = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Idioma",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = when(currentLanguage) {
                    "es" -> "Español"
                    "en" -> "English"
                    else -> "Español"
                },
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Guardar cambios",
            onClick = {
                sharedPrefs.edit().putString("username", editedUsername).apply()
                usernameState.value = editedUsername
                currentImageUri?.let { sharedPrefs.edit().putString("imageUri", it.toString()).apply() }
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentLanguage == "es",
                                onClick = { currentLanguage = "es" },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White
                                )
                            )
                            Text("Español", color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = currentLanguage == "en",
                                onClick = { currentLanguage = "en" },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White
                                )
                            )
                            Text("English", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Aplicar",
                        onClick = {
                            sharedPreferences.edit().putString("language", currentLanguage).apply()
                            setAppLocale(context, currentLanguage)
                            showLanguageDialog = false
                            context.restartApp()
                        }
                    )
                }
            )
        }
    }
}
