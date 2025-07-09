package com.example.capilux.screen

import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupScreen(
    navController: NavHostController,
    useAltTheme: Boolean
) {
    val context = LocalContext.current
    val prefs = EncryptedPrefs.get(context)

    // Comprobar soporte biométrico
    val biometricManager = BiometricManager.from(context)
    val deviceSupportsBiometrics = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    ) == BiometricManager.BIOMETRIC_SUCCESS

    // Estados del formulario
    var enableBiometric by rememberSaveable {
        mutableStateOf(
            prefs.getBoolean("useBiometric", deviceSupportsBiometrics && prefs.getBoolean("useBiometric", false))
        )
    }
    var pin by rememberSaveable { mutableStateOf(prefs.getString("pin", "") ?: "") }
    var showError by remember { mutableStateOf(false) }

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
            text = if (deviceSupportsBiometrics)
                "Activa el acceso por huella" else "Tu dispositivo no soporta biometría",
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (deviceSupportsBiometrics) {
            Switch(
                checked = enableBiometric,
                onCheckedChange = {
                    enableBiometric = it
                    prefs.edit().putBoolean("useBiometric", it).apply()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6A11CB)
                )
            )
        }

        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(4) },
            label = { Text("PIN", color = Color.White) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.padding(top = 24.dp),
            colors = TextFieldDefaults.colors(
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
                if (pin.length < 4) {
                    showError = true
                } else {
                    prefs.edit()
                        .putString("pin", pin)
                        .putBoolean("useBiometric", enableBiometric)
                        .apply()

                    navController.navigate("main") {
                        popUpTo("setupSecurity") { inclusive = true }
                    }
                }
            },
            enabled = pin.length == 4,
            modifier = Modifier.padding(top = 24.dp)
        )

        if (showError) {
            AlertDialog(
                onDismissRequest = { showError = false },
                title = { Text("Error") },
                text = { Text("El PIN debe tener 4 dígitos") },
                confirmButton = {
                    Button(onClick = { showError = false }) { Text("OK") }
                }
            )
        }
    }
}
