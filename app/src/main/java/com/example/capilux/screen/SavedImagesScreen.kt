package com.example.capilux.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import android.content.Context
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.deleteImageFile
import com.example.capilux.utils.saveImageToGallery

@Composable
fun SavedImagesScreen(navController: NavHostController, useAltTheme: Boolean) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("saved_images", Context.MODE_PRIVATE) }
    val images = remember { mutableStateListOf(*prefs.getStringSet("images", emptySet())!!.toTypedArray()) }
    val gradient = backgroundGradient(useAltTheme)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Im\u00e1genes guardadas", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atr\u00e1s", tint = Color.White)
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
                Text("No hay im\u00e1genes guardadas", color = Color.White)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(images) { index, uriString ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box {
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
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(8.dp)
                                        .align(Alignment.BottomEnd),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        saveImageToGallery(context, Uri.parse(uriString))
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
                        }
                    }
                }
            }
        }
    }
}
