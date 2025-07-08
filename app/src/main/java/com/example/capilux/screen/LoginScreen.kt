package com.example.capilux.screen

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val storedPin = prefs.getString("pin", "") ?: ""
    val useBiometric = prefs.getBoolean("useBiometric", false)
    val pinState = remember { mutableStateOf("") }
    val error = remember { mutableStateOf<String?>(null) }

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
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
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
            PrimaryButton(text = "Usar huella", onClick = {
                val activity = context as? FragmentActivity
                if (activity != null) {
                    showBiometric(activity)
                } else {
                    error.value = "No se pudo iniciar la autenticación"
                }
            }, modifier = Modifier.padding(top = 32.dp))
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
                if (pinState.value == storedPin && pinState.value.isNotEmpty()) {
                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                } else {
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
                confirmButton = { Button(onClick = { error.value = null }) { Text("OK") } }
            )
        }
    }
}
