package com.example.capilux.screen

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.components.CameraPreview
import com.example.capilux.navigation.getRecommendedFilters
import com.example.capilux.utils.takePhoto

@Composable
fun FilterPreviewScreen(faceShape: String, navController: NavHostController) {
    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }
    val filters = getRecommendedFilters(faceShape)
    val selectedIndex = remember { mutableStateOf(0) }
    val capturedUri = remember { mutableStateOf<android.net.Uri?>(null) }
    var isFrontCamera by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedUri.value == null) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraController = cameraController
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filters[selectedIndex.value])
            )

            IconButton(
                onClick = {
                    cameraController.cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    isFrontCamera = !isFrontCamera
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Cameraswitch, contentDescription = "Cambiar c\u00e1mara", tint = Color.White)
            }

            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                itemsIndexed(filters) { index, color ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(56.dp)
                            .background(color, CircleShape)
                            .clickable { selectedIndex.value = index }
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    takePhoto(
                        cameraController = cameraController,
                        context = context,
                        onSuccess = { uri -> capturedUri.value = uri },
                        onError = { }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                Icon(Icons.Filled.Camera, contentDescription = "Tomar foto")
            }
        } else {
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(capturedUri.value),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filters[selectedIndex.value])
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            ) {
                FloatingActionButton(onClick = { capturedUri.value = null }) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = "Reintentar")
                }
                Spacer(modifier = Modifier.size(16.dp))
                FloatingActionButton(
                    onClick = {
                        val prefs = context.getSharedPreferences("saved_images", Context.MODE_PRIVATE)
                        val set = prefs.getStringSet("images", emptySet())?.toMutableSet() ?: mutableSetOf()
                        capturedUri.value?.toString()?.let { set.add(it) }
                        prefs.edit().putStringSet("images", set).apply()
                        navController.navigate("savedImages")
                    }
                ) {
                    Icon(Icons.Filled.Camera, contentDescription = "Guardar")
                }
            }
        }
    }
}
