package com.example.capilux.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.navigation.getRecommendedStyles

@Composable
fun AnalysisResultScreen(faceShape: String, ratio: Float, navController: NavHostController) {
    val styles = getRecommendedStyles(faceShape)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tipo de rostro: $faceShape",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = String.format("Proporción ancho/alto: %.2f", ratio),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(styles) { style ->
                Text(style, color = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("filterPreview/$faceShape") }) {
            Text("Probar filtros")
        }
    }
}
