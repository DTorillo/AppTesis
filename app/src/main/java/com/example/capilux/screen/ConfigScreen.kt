package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.components.ProfileImageLarge
import com.example.capilux.ui.theme.*
import com.example.capilux.utils.compressImage
import com.example.capilux.utils.restartApp
import com.example.capilux.utils.setAppLocale
import com.example.capilux.utils.EncryptedPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    navController: NavHostController,
    usernameState: MutableState<String>,
    imageUri: Uri?,
    darkModeState: MutableState<Boolean>,
    altThemeState: MutableState<Boolean>
) {
    val context = LocalContext.current

    // 🔐 Preferencias seguras
    val sharedPrefs = remember { EncryptedPrefs.get(context) }

    // 🌓 Preferencias normales (no sensibles)
    val sharedPreferences = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var currentImageUri by remember { mutableStateOf(imageUri) }
    var altThemeEnabled by remember { mutableStateOf(altThemeState.value) }
    var currentLanguage by remember {
        mutableStateOf(sharedPreferences.getString("language", "es") ?: "es")
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val compressedUri = compressImage(context, it)
            currentImageUri = compressedUri
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
            Box(modifier = Modifier.clickable { galleryLauncher.launch("image/*") }) {
                ProfileImageLarge(imageUri = currentImageUri)
            }

            // Campo editable de nombre
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

            Text("Configuraciones", color = Color.White, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Degradado
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Degradado", color = Color.White)
                Switch(
                    checked = altThemeEnabled,
                    onCheckedChange = {
                        altThemeEnabled = it
                        altThemeState.value = it
                        sharedPreferences.edit().putBoolean("alt_theme_enabled", it).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        uncheckedThumbColor = Color.White,
                        checkedTrackColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }

            // Idioma
            var showLanguageDialog by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { showLanguageDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Idioma", color = Color.White)
                Text(
                    text = if (currentLanguage == "es") "Español" else "English",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Guardar cambios",
                onClick = {
                    sharedPrefs.edit().putString("username", editedUsername).apply()
                    usernameState.value = editedUsername
                    currentImageUri?.let {
                        sharedPrefs.edit().putString("imageUri", it.toString()).apply()
                    }
                    navController.popBackStack()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = "Volver",
                onClick = { navController.popBackStack() }
            )

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
                                    colors = RadioButtonDefaults.colors(
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
                                    colors = RadioButtonDefaults.colors(
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
                                context.restartApp()
                            }
                        )
                    }
                )
            }
        }
    }
}
