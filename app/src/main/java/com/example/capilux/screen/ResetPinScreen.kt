package com.example.capilux.screen

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
fun ResetPinScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    val questions = listOf(
        "¿Cuál es tu color favorito?",
        "¿Cómo se llama tu primera mascota?",
        "¿En qué ciudad naciste?",
        "¿Cuál es tu comida favorita?"
    )
    var pregunta by remember { mutableStateOf(questions.first()) }
    var expanded by remember { mutableStateOf(false) }
    var respuesta by remember { mutableStateOf("") }
    var verificado by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Recuperar PIN",
                color = Color.White,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = pregunta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pregunta de seguridad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(0.8f)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    questions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                pregunta = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = respuesta,
                onValueChange = { respuesta = it },
                label = { Text("Respuesta") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!verificado) {
                Button(
                    onClick = {
                        val correctQuestion = EncryptedPrefs.getSecurityQuestion(context)
                        val correct = EncryptedPrefs.isSecurityAnswerCorrect(context, respuesta)
                        if (pregunta == correctQuestion && correct) {
                            verificado = true
                            error = ""
                        } else {
                            error = "Datos incorrectos"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    Text("Verificar", fontSize = 18.sp)
                }
            } else {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("Nuevo PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6) confirmPin = it },
                    label = { Text("Confirmar PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                if (confirmPin.isNotEmpty() && pin != confirmPin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Los PIN no coinciden", color = Color.Red)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (pin.length == 6 && pin == confirmPin) {
                            EncryptedPrefs.savePin(context, pin)
                            EncryptedPrefs.saveLastPins(context, pin)
                            navController.navigate("auth") {
                                popUpTo("resetPin") { inclusive = true }
                            }
                        }
                    },
                    enabled = pin.length == 6 && pin == confirmPin,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    Text("Guardar PIN", fontSize = 18.sp)
                }
            }
        }
    }
}
