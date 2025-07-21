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
import androidx.compose.ui.text.input.KeyboardType
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
            EncryptedPrefs.setImageUri(context, compressedUri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuraci\u00f3n", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atr\u00e1s", tint = Color.White)
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
            var showChangePinDialog by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { showLanguageDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Idioma", color = Color.White)
                Text(
                    text = if (currentLanguage == "es") "Espa\u00f1ol" else "English",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { showChangePinDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cambiar PIN", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Guardar cambios",
                onClick = {
                    EncryptedPrefs.setUsername(context, editedUsername)
                    usernameState.value = editedUsername
                    currentImageUri?.let {
                        EncryptedPrefs.setImageUri(context, it.toString())
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
                                Text("Espa\u00f1ol", color = Color.White)
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

            if (showChangePinDialog) {
                var currentPin by remember { mutableStateOf("") }
                var newPin by remember { mutableStateOf("") }
                var confirmPin by remember { mutableStateOf("") }
                var error by remember { mutableStateOf("" ) }

                BaseDialog(
                    title = "Cambiar PIN",
                    onDismiss = { showChangePinDialog = false },
                    content = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedTextField(
                                value = currentPin,
                                onValueChange = { if (it.length <= 6) currentPin = it },
                                label = { Text("PIN actual", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { if (it.length <= 6) newPin = it },
                                label = { Text("Nuevo PIN", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = { if (it.length <= 6) confirmPin = it },
                                label = { Text("Confirmar PIN", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (error.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(error, color = Color.Red)
                            }
                        }
                    },
                    confirmButton = {
                        PrimaryButton(
                            text = "Guardar",
                            onClick = {
                                val savedPin = EncryptedPrefs.getPin(context)
                                if (currentPin == savedPin && newPin.length == 6 && newPin == confirmPin) {
                                    EncryptedPrefs.savePin(context, newPin)
                                    EncryptedPrefs.saveLastPins(context, newPin)
                                    showChangePinDialog = false
                                } else {
                                    error = "Datos incorrectos"
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
