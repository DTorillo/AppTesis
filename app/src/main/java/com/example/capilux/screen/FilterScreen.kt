package com.example.capilux.screen

import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.components.CameraPreview
import com.example.capilux.navigation.getRecommendedStyles

@Composable
fun FilterScreen(navController: NavHostController, faceShape: String) {
    val context = LocalContext.current
    val filters = getRecommendedStyles(faceShape)
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
    var selectedIndex by remember { mutableStateOf(0) }
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize(), cameraController = cameraController)
        // Aplicar un color simple como filtro
        val overlayColor = when (selectedIndex % filters.size) {
            0 -> Color.Transparent
            1 -> Color(0x330000FF)
            2 -> Color(0x3300FF00)
            else -> Color(0x33FF0000)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEachIndexed { index, name ->
                Button(onClick = { selectedIndex = index }, modifier = Modifier.padding(4.dp)) {
                    Text(name)
                }
            }
        }
    }
}
