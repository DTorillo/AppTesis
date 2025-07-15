package com.example.capilux.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.R // Asegúrate de tener un recurso de logo
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun WelcomeScreen(navController: NavHostController, useAltTheme: Boolean) {
    val gradient = backgroundGradient(useAltTheme)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo de la app
        Image(
            painter = painterResource(id = R.drawable.logo), // Crea un recurso drawable
            contentDescription = "Logo Capilux",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjeta de bienvenida
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.1f),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a Capilux",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    buildAnnotatedString {
                        append("Descubre el ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("corte perfecto")
                        }
                        append(" para tu tipo de rostro con nuestra tecnología de análisis facial")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lista de beneficios
                listOf(
                    "Toma o sube una foto de tu rostro",
                    "Analizamos tu estructura facial",
                    "Recibe recomendaciones personalizadas"
                ).forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Face,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "${index + 1}. $item",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de inicio
        PrimaryButton(
            text = "Comenzar ahora",
            onClick = { navController.navigate("userCreation") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Texto adicional
        Text(
            text = "¡Tu estilo perfecto está a solo un clic!",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.height(32.dp))


}
