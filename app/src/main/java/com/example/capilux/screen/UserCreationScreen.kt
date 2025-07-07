package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.R
import com.example.capilux.components.TermsAndConditionsDialog
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCreationScreen(navController: NavHostController, useAltTheme: Boolean, usernameState: MutableState<String>) {
    var username by remember { mutableStateOf("") }
    var isTermsAccepted by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val gradient = backgroundGradient(useAltTheme)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Encabezado
            Text(
                text = "Personaliza tu experiencia",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Crea tu perfil para recomendaciones personalizadas",
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Foto de perfil
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.1f),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Agregar foto",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agregar foto",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Términos y condiciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        checkmarkColor = Color(0xFF6A11CB)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    buildAnnotatedString {
                        append("Acepto los ")
                        withStyle(style = SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )) {
                            append("términos y condiciones")
                        }
                    },
                    color = Color.White,
                    modifier = Modifier.clickable { showTermsDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Continuar",
                onClick = {
                    when {
                        username.isBlank() -> {
                            dialogMessage = "Por favor, introduce un nombre de usuario."
                            showDialog = true
                        }
                        !isTermsAccepted -> {
                            dialogMessage = "Por favor, acepta los términos y condiciones."
                            showDialog = true
                        }
                        else -> {
                            // Guardar en SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putString("username", username)
                            imageUri?.let { editor.putString("imageUri", it.toString()) }
                            editor.apply()

                            // Actualizar estado de usuario y navegar
                            usernameState.value = username
                            navController.navigate("setupSecurity")
                        }
                    }
                },
                enabled = username.isNotBlank() && isTermsAccepted
            )

            // Diálogos
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Atención") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showTermsDialog) {
                TermsAndConditionsDialog(
                    onDismiss = { showTermsDialog = false },
                    useAltTheme = useAltTheme
                )
            }
        }
    }
    }