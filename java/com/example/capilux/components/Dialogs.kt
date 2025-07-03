package com.example.capilux.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.capilux.ui.theme.PrimaryButton

@Composable
fun TermsAndConditionsDialog(onDismiss: () -> Unit) {
    // Gradiente para el fondo principal
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
        startY = 0f,
        endY = 1000f
    )

    // Estado para el scroll
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(min = 500.dp, max = 650.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF2D0C5A), // Fondo sólido púrpura oscuro
            border = BorderStroke(2.dp, Color.White),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient) // Fondo con gradiente para todo el diálogo
            ) {
                // Encabezado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D0C5A))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Términos y Condiciones",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Contenido principal
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Al utilizar Capilux, aceptas los siguientes términos:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Términos con iconos
                    listOf(
                        Pair(Icons.Filled.Security, "Privacidad de datos: Tus imágenes se procesan localmente en tu dispositivo y no se comparten con servidores externos."),
                        Pair(Icons.Filled.CloudUpload, "Propiedad intelectual: Conservas todos los derechos sobre tus fotos y los resultados del análisis."),
                        Pair(Icons.Filled.Info, "Uso de la aplicación: Capilux es una herramienta de recomendación. Los resultados son sugerencias basadas en análisis facial."),
                        Pair(Icons.Filled.Gavel, "Limitación de responsabilidad: No nos hacemos responsables por decisiones basadas en las recomendaciones proporcionadas."),
                        Pair(Icons.Filled.Update, "Actualizaciones: Podemos modificar estos términos periódicamente. Las versiones actualizadas estarán disponibles en la aplicación."),
                        Pair(Icons.Filled.Block, "Uso apropiado: Te comprometes a no utilizar la aplicación para fines ilegales o que violen derechos de terceros.")
                    ).forEach { (icon, term) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x55441199)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(end = 12.dp)
                                )
                                Text(
                                    text = term,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Texto final
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x553366FF)
                        )
                    ) {
                        Text(
                            text = "Al hacer clic en 'Aceptar', confirmas que has leído, comprendido y aceptado estos términos y condiciones en su totalidad.",
                            color = Color.White,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Botón de aceptar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PrimaryButton(
                        text = "Acepto los términos",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}
