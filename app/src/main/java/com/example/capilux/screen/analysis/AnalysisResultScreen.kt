package com.example.capilux.screen.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun AnalysisResultScreen(
    resultado: String,
    imageUri: String,
    navController: NavHostController,
    useAltTheme: Boolean
) {
    val gradient = backgroundGradient(useAltTheme)

    // Dividir en líneas
    val lineas = resultado.trim().lines().filter { it.isNotBlank() }

    // Detectar tipo de rostro (primera línea que contenga "Forma del rostro:")
    val tipo = lineas.firstOrNull { it.contains("Forma del rostro:", ignoreCase = true) }
        ?.split(":")
        ?.getOrNull(1)
        ?.trim()
        ?.lowercase()
        ?: "desconocido"

    // Extraer el tiempo si existe
    val tiempo = lineas.firstOrNull { it.contains("⏱") }

    // El resto son detalles, quitando tipo y tiempo
    val detalles = lineas.filterNot {
        it.contains("Forma del rostro:", ignoreCase = true) || it.contains("⏱")
    }

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
            style = MaterialTheme.typography.headlineSmall
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
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        tiempo?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(detalles) { _, medida ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = medida,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("maskProcessingScreen/${android.net.Uri.encode(imageUri)}")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Crear máscara",
                color = Color(0xFF2D0C5A),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}
