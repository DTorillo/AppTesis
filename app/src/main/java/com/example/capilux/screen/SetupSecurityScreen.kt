
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
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs

@Composable
fun SetupSecurityScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val preguntas = listOf(
        "¿Nombre de tu primera mascota?",
        "¿Ciudad donde naciste?",
        "¿Comida favorita?",
        "¿Nombre de tu mejor amigo(a) de la infancia?"
    )

    var pregunta by remember { mutableStateOf(preguntas[0]) }
    var expandPreguntas by remember { mutableStateOf(false) }
    var respuesta by remember { mutableStateOf("") }
    var activarHuella by remember { mutableStateOf(false) }
    val puedeUsarHuella = remember {
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    val textColor = Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Configura tu seguridad",
                color = textColor,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text("Elige un PIN de 6 dígitos", color = textColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = textColor.copy(alpha = 0.5f),
                    cursorColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 6) confirmPin = it },
                label = { Text("Confirma tu PIN", color = textColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = textColor.copy(alpha = 0.5f),
                    cursorColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (confirmPin.isNotEmpty() && confirmPin != pin) {
                Text(
                    text = "Los PIN no coinciden",
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (puedeUsarHuella) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = activarHuella,
                        onCheckedChange = { activarHuella = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = textColor,
                            uncheckedColor = textColor,
                            checkmarkColor = Color(0xFF6A11CB)
                        )
                    )
                    Text("Activar acceso con huella", color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandPreguntas,
                onExpandedChange = { expandPreguntas = !expandPreguntas }
            ) {
                OutlinedTextField(
                    value = pregunta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pregunta de seguridad", color = textColor) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandPreguntas)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(0.8f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                        focusedIndicatorColor = textColor,
                        unfocusedIndicatorColor = textColor.copy(alpha = 0.5f),
                        cursorColor = textColor
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandPreguntas,
                    onDismissRequest = { expandPreguntas = false }
                ) {
                    preguntas.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = textColor) },
                            onClick = {
                                pregunta = option
                                expandPreguntas = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = respuesta,
                onValueChange = { respuesta = it },
                label = { Text("Respuesta secreta", color = textColor) },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                    focusedIndicatorColor = textColor,
                    unfocusedIndicatorColor = textColor.copy(alpha = 0.5f),
                    cursorColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Finalizar",
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
                enabled = pin.length == 6 && pin == confirmPin &&
                    pregunta.isNotBlank() && respuesta.isNotBlank(),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}
