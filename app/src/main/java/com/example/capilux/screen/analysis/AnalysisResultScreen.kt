package com.example.capilux.screen.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capilux.ui.theme.backgroundGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    resultado: String,
    imageUri: String,
    navController: NavHostController,
    useAltTheme: Boolean
) {
    val gradient = backgroundGradient(useAltTheme)

    // Procesamiento de datos
    val lineas = resultado.trim().lines().filter { it.isNotBlank() }
    val tipo = lineas.firstOrNull { it.contains("Forma del rostro:", ignoreCase = true) }
        ?.split(":")
        ?.getOrNull(1)
        ?.trim()
        ?.lowercase()
        ?: "desconocido"
    val tiempo = lineas.firstOrNull { it.contains("⏱") }
    val detalles = lineas.filterNot {
        it.contains("Forma del rostro:", ignoreCase = true) || it.contains("⏱")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Resultados del Análisis",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de tipo de rostro
                Spacer(modifier = Modifier.height(16.dp))

                FaceTypeCard(tipo)

                Spacer(modifier = Modifier.height(24.dp))

                // Tiempo de procesamiento
                tiempo?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Detalles del análisis
                Text(
                    text = "Detalles del análisis:",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.Start)
                )

                AnalysisDetailsList(detalles)

                // Botón de acción
                Spacer(modifier = Modifier.height(16.dp))
                CreateMaskButton(navController, imageUri)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FaceTypeCard(tipo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Círculo con la letra (sin card de fondo)
        Box(
            modifier = Modifier
                .size(120.dp)  // Tamaño aumentado para mejor visibilidad
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6A35FF).copy(alpha = 0.9f),
                            Color(0xFF3A1C99).copy(alpha = 0.9f)
                        ),
                        center = Offset(0.5f, 0.5f),
                        radius = 0.8f
                    )
                )
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = Color(0xFF6A35FF).copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tipo.first().uppercase(),
                color = Color.White,
                fontSize = 48.sp,  // Tamaño aumentado
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = (-2).dp)  // Ajuste fino de centrado vertical
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Texto descriptivo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tipo de rostro",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.titleSmall,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = tipo.replaceFirstChar { it.uppercase() },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
@Composable
private fun ColumnScope.AnalysisDetailsList(detalles: List<String>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
    ) {
        itemsIndexed(detalles) { index, medida ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6A35FF))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = medida,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateMaskButton(navController: NavHostController, imageUri: String) {
    Button(
        onClick = {
            navController.navigate("maskProcessingScreen/${android.net.Uri.encode(imageUri)}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF2D0C5A)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = "Crear Máscara Personalizada",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
