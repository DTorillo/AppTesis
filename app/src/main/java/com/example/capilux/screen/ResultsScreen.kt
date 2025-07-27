package com.example.capilux.screen

import android.net.Uri
import androidx.compose.foundation.Image
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.SecondaryButton
import com.example.capilux.ui.theme.backgroundGradient
import java.io.File

@Composable
fun ResultsScreen(
    faceShape: String,
    recommendedStyles: List<String>,
    imageUri: Uri?,
    useAltTheme: Boolean
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("favorites", Context.MODE_PRIVATE) }
    val userPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val generatedPath = userPrefs.getString("generated_image_path", null)
    val generatedUri = generatedPath?.let { Uri.fromFile(File(it)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient(useAltTheme))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu tipo de rostro es: $faceShape",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = "Foto analizada",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        generatedUri?.let { uri ->
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Estilos recomendados:",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(recommendedStyles) { style ->
                Text(
                    text = style,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Volver", color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Guardar resultados",
            onClick = {
                val current = prefs.getStringSet("styles", emptySet())?.toMutableSet() ?: mutableSetOf()
                current.addAll(recommendedStyles)
                prefs.edit().putStringSet("styles", current).apply()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            text = "Volver",
            onClick = { navController.popBackStack() }
        )
    }
}