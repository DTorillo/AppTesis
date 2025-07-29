package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.deleteImageFile
import com.example.capilux.utils.saveImageToGallery
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedImagesScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("saved_images", Context.MODE_PRIVATE)
    }
    val images = remember {
        mutableStateListOf(*prefs.getStringSet("images", emptySet())?.toTypedArray() ?: emptyArray())
    }
    val gradient = backgroundGradient(useAltTheme)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Imágenes guardadas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (images.isEmpty()) {
                Text("No hay imágenes guardadas", color = Color.White)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(images) { uriString ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color.White, RectangleShape)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uriString),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    val file = uriToFile(context, Uri.parse(uriString))
                                    saveImageToGallery(context, file)
                                }) {
                                    Icon(Icons.Filled.Download, contentDescription = "Descargar", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    deleteImageFile(context, uriString)
                                    images.remove(uriString)
                                    prefs.edit().putStringSet("images", images.toSet()).apply()
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ✅ Función para convertir Uri a File (usa almacenamiento temporal)
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "temp_from_gallery.jpg")
    inputStream?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}
