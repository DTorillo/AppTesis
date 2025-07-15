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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.components.CameraPreview
import com.example.capilux.navigation.getRecommendedFilters

@Composable
fun FilterPreviewScreen(faceShape: String, navController: NavHostController) {
    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    val filters = getRecommendedFilters(faceShape)
    val selectedIndex = remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraController = cameraController
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(filters[selectedIndex.value])
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            filters.forEachIndexed { index, _ ->
                Button(onClick = { selectedIndex.value = index }) {
                    Text("Filtro ${index + 1}")
                }
            }
        }
    }
}
