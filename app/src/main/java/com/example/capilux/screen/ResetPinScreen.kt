package com.example.capilux.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.gradientTextFieldColors
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.capilux.utils.EncryptedPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPinScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    val preguntas = listOf(
        "¿Nombre de tu primera mascota?",
        "¿Ciudad donde naciste?",
        "¿Comida favorita?",
        "¿Nombre de tu mejor amigo(a) de la infancia?"
    )

    var pregunta by remember { mutableStateOf(preguntas[0]) }
    var expandPreguntas by remember { mutableStateOf(false) }
    var respuesta by remember { mutableStateOf("") }
    var nuevoPin by remember { mutableStateOf("") }
    var confirmarPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Recuperar PIN",
                color = Color.White,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(
                expanded = expandPreguntas,
                onExpandedChange = { expandPreguntas = !expandPreguntas }
            ) {
                OutlinedTextField(
                    value = pregunta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pregunta elegida") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandPreguntas)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(16.dp),
                    colors = gradientTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expandPreguntas,
                    onDismissRequest = { expandPreguntas = false }
                ) {
                    preguntas.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White) },
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
                label = { Text("Respuesta") },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nuevoPin,
                onValueChange = { if (it.length <= 6) nuevoPin = it },
                label = { Text("Nuevo PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmarPin,
                onValueChange = { if (it.length <= 6) confirmarPin = it },
                label = { Text("Confirmar PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Guardar",
                onClick = {
                    if (pregunta == EncryptedPrefs.getSecurityQuestion(context) &&
                        EncryptedPrefs.isSecurityAnswerCorrect(context, respuesta) &&
                        nuevoPin.length == 6 && nuevoPin == confirmarPin
                    ) {
                        EncryptedPrefs.savePin(context, nuevoPin)
                        EncryptedPrefs.saveLastPins(context, nuevoPin)
                        navController.navigate("auth") {
                            popUpTo("resetPin") { inclusive = true }
                        }
                    } else {
                        error = "Datos incorrectos"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
        }
    }
}
