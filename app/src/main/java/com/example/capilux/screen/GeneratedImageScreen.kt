package com.example.capilux.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.SharedViewModel
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.saveImageToGallery
import androidx.compose.ui.platform.LocalContext

@Composable
fun GeneratedImageScreen(imageUri: String, navController: NavHostController, sharedViewModel: SharedViewModel) {
    val uri = Uri.parse(imageUri)
    val gradient = backgroundGradient(useAltTheme = true)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = uri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                sharedViewModel.clear()
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }) {
                Text("Volver a tomar otra foto")
            }
            Button(onClick = { saveImageToGallery(context, uri) }) {
                Text("Guardar imagen")
            }
        }
    }
}
