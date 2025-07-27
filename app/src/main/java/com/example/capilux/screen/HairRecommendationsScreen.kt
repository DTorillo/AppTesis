package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.navigation.getRecommendedStyles
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.network.ServerApi
import java.io.File

@Composable
fun HairRecommendationsScreen(
    faceShape: String,
    imageUri: String?,
    navController: NavHostController,
    useAltTheme: Boolean
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val originalUri = imageUri?.let { Uri.parse(it) }
    val styles = getRecommendedStyles(faceShape)
    val gradient = backgroundGradient(useAltTheme)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recomendaciones", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            styles.forEach { style ->
                Button(
                    onClick = {
                        originalUri?.let { uri ->
                            ServerApi.enviarImagen(
                                context = context,
                                imageUri = uri,
                                prompt = style,
                                onResult = { base64 ->
                                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                                    val file = File(context.cacheDir, "generated_result.png")
                                    file.writeBytes(bytes)
                                    prefs.edit().putString("generated_image_path", file.absolutePath).apply()
                                    val encoded = Uri.encode(imageUri ?: "")
                                    navController.navigate("results/$faceShape") {
                                        popUpTo("hairRecommendations/$faceShape/$encoded") { inclusive = true }
                                    }
                                },
                                onError = { }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x552D0C5A))
                ) {
                    Text(style, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
