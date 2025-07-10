package com.example.capilux.screen

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.capilux.ui.theme.backgroundGradient
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun BiometricSetupScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    var status by remember { mutableStateOf("Coloca tu dedo para acceder") }

    val executor: Executor = Executors.newSingleThreadExecutor()
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Autenticación Biométrica")
        .setSubtitle("Verifica tu identidad con huella")
        .setNegativeButtonText("Cancelar")
        .build()

    val biometricPrompt = BiometricPrompt(
        context as androidx.fragment.app.FragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                status = "Autenticación exitosa"
                navController.navigate("main")
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                status = "Error: $errString"
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                status = "Huella no reconocida"
            }
        })

    LaunchedEffect(Unit) {
        biometricPrompt.authenticate(promptInfo)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verificación biométrica",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
