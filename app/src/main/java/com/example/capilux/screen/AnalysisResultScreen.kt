package com.example.capilux.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient
import kotlinx.coroutines.delay

@Composable
fun AnalysisResultScreen(resultado: String, navController: NavHostController) {
    val lineas = resultado.trim().lines().filter { it.isNotBlank() }
    val tipo = lineas.firstOrNull { it.contains("Forma del rostro:") }?.split(":")?.getOrNull(1)?.trim()?.lowercase()
        ?: "desconocido"
    val tiempo = lineas.find { it.contains("segundo") || it.contains("segundos") }
    val soloMedidas = lineas.drop(1).filterNot { it.contains("segundo") }

    val gradient = backgroundGradient(useAltTheme = true)
    val cardColor = getCardColorForFaceType(tipo)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resultados del análisis facial",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tipo de rostro detectado:",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = tipo.replaceFirstChar { it.uppercase() },
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        tiempo?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⏱ Tiempo de análisis: $it",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(soloMedidas) { index, medida ->
                val (icon, iconColor) = getIconAndColor(medida)
                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(index) {
                    delay(100L * index)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + expandVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = medida,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("filterPreview/$tipo") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Probar filtros sugeridos",
                color = Color(0xFF2D0C5A),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getCardColorForFaceType(tipo: String): Color {
    return when (tipo.lowercase()) {
        "ovalado" -> Color(0xFF80CBC4)
        "cuadrado" -> Color(0xFFCE93D8)
        "alargado" -> Color(0xFFFFAB91)
        "triangular" -> Color(0xFF90CAF9)
        "redondo" -> Color(0xFFFFF59D)
        "diamante" -> Color(0xFFB39DDB)
        "corazon" -> Color(0xFFA5D6A7)
        "rectangular" -> Color(0xFFFFCC80)
        "trapecio" -> Color(0xFFB0BEC5)
        else -> Color.DarkGray
    }
}

private fun getIconAndColor(texto: String): Pair<ImageVector, Color> {
    return when {
        texto.contains("Ancho rostro", ignoreCase = true) -> Icons.Filled.ZoomOutMap to Color(0xFF80CBC4)
        texto.contains("Alto rostro", ignoreCase = true) -> Icons.Filled.Straighten to Color(0xFFCE93D8)
        texto.contains("Largo nariz", ignoreCase = true) -> Icons.Filled.Sensors to Color(0xFFA5D6A7)
        texto.contains("Mandíbula", ignoreCase = true) -> Icons.Filled.AccountCircle to Color(0xFFFFAB91)
        texto.contains("Frente", ignoreCase = true) && !texto.contains("↕️") -> Icons.Filled.WbSunny to Color(0xFFFFF59D)
        texto.contains("Frente a mentón", ignoreCase = true) -> Icons.Filled.South to Color(0xFF90CAF9)
        texto.contains("Distancia entre ojos", ignoreCase = true) -> Icons.Filled.RemoveRedEye to Color(0xFFFFCC80)
        texto.contains("Proporción", ignoreCase = true) -> Icons.Filled.BarChart to Color(0xFFB39DDB)
        else -> Icons.Filled.Info to Color.White
    }
}
