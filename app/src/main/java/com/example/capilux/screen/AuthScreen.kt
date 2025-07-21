
package com.example.capilux.screen

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.example.capilux.R
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.gradientTextFieldColors
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.capilux.utils.EncryptedPrefs
import java.util.concurrent.Executor

@Composable
fun AuthScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val gradient = backgroundGradient(useAltTheme)
    var pin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Verificando huella...") }
    var showPin by remember { mutableStateOf(false) }
    val executor: Executor = ContextCompat.getMainExecutor(context)

    // Biometría solo si está activada por el usuario
    LaunchedEffect(Unit) {
        if (EncryptedPrefs.canUseBiometrics(context)) {
            val manager = BiometricManager.from(context)
            if (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS) {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verificación biométrica")
                    .setSubtitle("Coloca tu dedo")
                    .setNegativeButtonText("Usar PIN")
                    .build()

                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }

                        override fun onAuthenticationError(code: Int, msg: CharSequence) {
                            status = "Autenticación cancelada. Usa tu PIN"
                            showPin = true
                        }

                        override fun onAuthenticationFailed() {
                            status = "Huella no reconocida. Intenta otra vez"
                        }
                    })

                biometricPrompt.authenticate(promptInfo)
            } else {
                status = "Huella no disponible. Usa tu PIN"
                showPin = true
            }
        } else {
            status = "Huella no configurada. Usa tu PIN"
            showPin = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Bienvenido a Capilux",
                fontSize = 22.sp,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = status, color = Color.White.copy(alpha = 0.7f))

            if (showPin) {
                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .width(240.dp)
                        .padding(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = gradientTextFieldColors(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    onClick = {
                        if (pin == EncryptedPrefs.getPin(context)) {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } else {
                            status = "PIN incorrecto"
                            pin = ""
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = {
                    navController.navigate("resetPin")
                }) {
                    Text("¿Olvidaste tu PIN?", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
