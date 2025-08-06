
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.ui.theme.gradientTextFieldColors
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.capilux.utils.EncryptedPrefs
import com.example.capilux.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupSecurityScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val preguntas = listOf(
        stringResource(R.string.security_question_pet),
        stringResource(R.string.security_question_city),
        stringResource(R.string.security_question_food),
        stringResource(R.string.security_question_friend)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.setup_security_title),
                color = Color.White,
                fontSize = 22.sp,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 6) pin = it },
                label = { Text(stringResource(R.string.choose_pin_6_digits)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 6) confirmPin = it },
                label = { Text(stringResource(R.string.confirm_pin)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (confirmPin.isNotEmpty() && confirmPin != pin) {
                Text(
                    text = stringResource(R.string.pin_mismatch),
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
                            checkedColor = Color.White,
                            uncheckedColor = Color.White,
                            checkmarkColor = Color(0xFF6A11CB)
                        )
                    )
                    Text(stringResource(R.string.enable_fingerprint_access), color = Color.White)
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
                    label = { Text(stringResource(R.string.security_question_label)) },
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
                label = { Text(stringResource(R.string.secret_answer)) },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = gradientTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.finish),
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
