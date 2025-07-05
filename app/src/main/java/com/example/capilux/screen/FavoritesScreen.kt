package com.example.capilux.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesScreen() {
    // Lista simulada de estilos favoritos
    val favoriteStyles = listOf(
        "Corte clásico",
        "Peinado hacia atrás",
        "Corte degradado",
        "Undercut",
        "Flequillo"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Estilos Favoritos",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(favoriteStyles) { style ->
                Text(
                    text = style,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Acción para guardar o modificar favoritos
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Guardar favoritos")
        }
    }
}
