package com.example.capilux.screen

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.example.capilux.components.showErrorDialog
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun LoginScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val sharedPrefs = EncryptedPrefs.get(context)
    val biometricExecutor: Executor = Executors.newSingleThreadExecutor()

    var pin by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val savedPin = sharedPrefs.getString("pin", null)
    val useBiometric = sharedPrefs.getBoolean("useBiometric", false)

    val biometricAvailable = remember {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    val promptInfo by rememberUpdatedState(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Usa tu huella digital para acceder")
            .setNegativeButtonText("Cancelar")
            .build()
    )

    val biometricPrompt = BiometricPrompt(
        context as FragmentActivity,
        ContextCompat.getMainExecutor(context), // ✅ ejecuta en UI thread
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                errorMessage = "Error: $errString"
                showDialog = true
            }

            override fun onAuthenticationFailed() {
                errorMessage = "Huella no reconocida. Inténtalo de nuevo."
                showDialog = true
            }
        }
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient(useAltTheme)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Autenticación", style = MaterialTheme.typography.titleLarge, color = Color.White)

            // ✅ Solo muestra si hay PIN y biometría activa
            if (!savedPin.isNullOrEmpty() && useBiometric && biometricAvailable) {
                Button(onClick = {
                    biometricPrompt.authenticate(promptInfo)
                }) {
                    Text("Usar huella")
                }
            }

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                )
            )

            PrimaryButton(
                text = "Entrar",
                onClick = {
                    if (pin == savedPin) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        errorMessage = "PIN incorrecto"
                        showDialog = true
                    }
                }
            )
        }
    }

    if (showDialog) {
        showErrorDialog(
            message = errorMessage,
            onDismiss = { showDialog = false }
        )
    }
}
