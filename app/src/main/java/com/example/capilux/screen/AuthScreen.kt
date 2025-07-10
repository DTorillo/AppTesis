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
import java.util.concurrent.Executor

@Composable
fun AuthScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val gradient = backgroundGradient(useAltTheme)
    var pin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Verificando identidad...") }
    val correctPin = "123456"
    var showPin by remember { mutableStateOf(false) }
    val executor: Executor = ContextCompat.getMainExecutor(context)

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Coloca tu dedo para continuar")
            .setNegativeButtonText("Usar PIN")
            .build()
    }

    // Intenta usar huella automáticamente al abrir
    LaunchedEffect(Unit) {
        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            == BiometricManager.BIOMETRIC_SUCCESS) {

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        status = "Huella verificada"
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        status = "Huella no reconocida"
                    }

                    override fun onAuthenticationError(code: Int, msg: CharSequence) {
                        status = "Usa tu PIN para continuar"
                        showPin = true
                    }
                })

            biometricPrompt.authenticate(promptInfo)
        } else {
            status = "Huella no disponible. Usa tu PIN"
            showPin = true
        }
    }

    // UI
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = status,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )

            if (showPin) {
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("PIN", color = Color.White) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 22.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .width(220.dp)
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (pin == correctPin) {
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
                ) {
                    Text("Acceder", fontSize = 18.sp)
                }
            }
        }
    }
}

