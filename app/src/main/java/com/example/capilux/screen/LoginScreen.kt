package com.example.capilux.screen

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val prefs = EncryptedPrefs.get(context)

    val storedPin = prefs.getString("pin", "") ?: ""
    val useBiometric = prefs.getBoolean("useBiometric", false)
    val pinState = remember { mutableStateOf("") }
    val error = remember { mutableStateOf<String?>(null) }

    // Protección: máximo 3 intentos
    val maxAttempts = 3
    val cooldownTime = 30_000L // 30 segundos
    val lastAttemptTime = prefs.getLong("last_attempt_time", 0L)
    val failedAttempts = prefs.getInt("failed_attempts", 0)
    val now = System.currentTimeMillis()
    val isLocked = failedAttempts >= maxAttempts && (now - lastAttemptTime < cooldownTime)

    val gradient = backgroundGradient(useAltTheme)

    fun showBiometric(activity: FragmentActivity) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    error.value = errString.toString()
                }
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso biométrico")
            .setNegativeButtonText("Cancelar")
            .build()
        prompt.authenticate(info)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Autenticación") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )

        if (useBiometric) {
            PrimaryButton(
                text = "Usar huella",
                onClick = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        showBiometric(activity)
                    } else {
                        error.value = "No se pudo iniciar la autenticación"
                    }
                },
                modifier = Modifier.padding(top = 32.dp)
            )
        }

        OutlinedTextField(
            value = pinState.value,
            onValueChange = { pinState.value = it.filter { ch -> ch.isDigit() }.take(4) },
            label = { Text("PIN", color = Color.White) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White
            ),
            modifier = Modifier.padding(top = 24.dp)
        )

        PrimaryButton(
            text = "Entrar",
            onClick = {
                if (isLocked) {
                    error.value = "Has excedido los intentos. Espera unos segundos."
                } else if (pinState.value == storedPin && pinState.value.isNotEmpty()) {
                    prefs.edit().putInt("failed_attempts", 0).apply()
                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                } else {
                    prefs.edit()
                        .putInt("failed_attempts", failedAttempts + 1)
                        .putLong("last_attempt_time", now)
                        .apply()
                    error.value = "PIN incorrecto"
                }
            },
            enabled = pinState.value.length == 4,
            modifier = Modifier.padding(top = 24.dp)
        )

        error.value?.let { msg ->
            AlertDialog(
                onDismissRequest = { error.value = null },
                title = { Text("Error") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = { error.value = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
