package com.example.capilux.auth

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val biometricManager = BiometricManager.from(context)

    var showPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showPin || biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
        PinScreen(onAuthenticated)
    } else {
        LaunchedEffect(Unit) {
            authenticateBiometric(activity, onAuthenticated) { errorMessage = it }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Autenticación biométrica")
                errorMessage?.let { Text(it) }
                TextButton(onClick = { showPin = true }) {
                    Text("Usar PIN")
                }
            }
        }
    }
}

private fun authenticateBiometric(activity: FragmentActivity, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Iniciar sesión")
        .setSubtitle("Escanea tu huella digital")
        .setNegativeButtonText("Cancelar")
        .build()
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError(errString.toString())
        }
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onError("Autenticación fallida")
        }
    })
    biometricPrompt.authenticate(promptInfo)
}

@Composable
fun PinScreen(onAuthenticated: () -> Unit, correctPin: String = "1234") {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Introduce PIN")
        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN") })
        if (error) {
            Text("PIN incorrecto")
        }
        Button(onClick = {
            if (pin == correctPin) {
                onAuthenticated()
            } else {
                error = true
            }
        }) {
            Text("Entrar")
        }
    }
}

