
package com.example.capilux.screen

import androidx.biometric.BiometricManager
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs

@Composable
fun SetupSecurityScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    var pin by remember { mutableStateOf("") }
    var pregunta by remember { mutableStateOf("") }
    var respuesta by remember { mutableStateOf("") }
    var activarHuella by remember { mutableStateOf(false) }
    val puedeUsarHuella = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Configura tu seguridad",
                color = Color.White,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("Elige un PIN de 6 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (puedeUsarHuella) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = activarHuella,
                        onCheckedChange = { activarHuella = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color.White)
                    )
                    Text("Activar acceso con huella", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pregunta,
                onValueChange = { pregunta = it },
                label = { Text("Pregunta de seguridad") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = respuesta,
                onValueChange = { respuesta = it },
                label = { Text("Respuesta secreta") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pin.length == 6 && pregunta.isNotBlank() && respuesta.isNotBlank()) {
                        EncryptedPrefs.savePin(context, pin)
                        EncryptedPrefs.saveLastPins(context, pin)
                        EncryptedPrefs.setUseBiometrics(context, activarHuella)
                        EncryptedPrefs.setSecurityQuestion(context, pregunta, respuesta)
                        EncryptedPrefs.setSetupDone(context, true)
                        navController.navigate("main") {
                            popUpTo("setupSecurity") { inclusive = true }
                        }
                    }
                },
                enabled = pin.length == 6 && pregunta.isNotBlank() && respuesta.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(50.dp)
            ) {
                Text("Finalizar", fontSize = 18.sp)
            }
        }
    }
}
