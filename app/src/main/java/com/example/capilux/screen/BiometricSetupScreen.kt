package com.example.capilux.screen

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val biometricManager = BiometricManager.from(context)
    val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            == BiometricManager.BIOMETRIC_SUCCESS

    val enableBiometric = remember { mutableStateOf(canAuth) }
    val pinState = remember { mutableStateOf("") }
    val showError = remember { mutableStateOf(false) }

    val gradient = backgroundGradient(useAltTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Seguridad") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )
        Text(
            text = if (canAuth) "Activa el acceso por huella" else "Tu dispositivo no soporta biometría",
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        if (canAuth) {
            Switch(
                checked = enableBiometric.value,
                onCheckedChange = { enableBiometric.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6A11CB)
                )
            )
        }
        OutlinedTextField(
            value = pinState.value,
            onValueChange = { pinState.value = it.filter { ch -> ch.isDigit() }.take(4) },
            label = { Text("PIN", color = Color.White) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White
            )
        )
        PrimaryButton(
            text = "Guardar",
            onClick = {
                if (pinState.value.length < 4) {
                    showError.value = true
                } else {
                    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("pin", pinState.value)
                        .putBoolean("useBiometric", enableBiometric.value)
                        .apply()
                    navController.navigate("main") { popUpTo("setupSecurity") { inclusive = true } }
                }
            },
            enabled = pinState.value.length == 4,
            modifier = Modifier.padding(top = 24.dp)
        )
        if (showError.value) {
            AlertDialog(
                onDismissRequest = { showError.value = false },
                title = { Text("Error") },
                text = { Text("El PIN debe tener 4 dígitos") },
                confirmButton = {
                    Button(onClick = { showError.value = false }) { Text("OK") }
                }
            )
        }
    }
}
