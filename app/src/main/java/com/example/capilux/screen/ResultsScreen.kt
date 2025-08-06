package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.ui.theme.PrimaryButton
import com.example.capilux.ui.theme.SecondaryButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.uriToFile
import com.example.capilux.R
import com.example.capilux.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun ResultsScreen(
    faceShape: String,
    recommendedStyles: List<String>,
    imageUri: Uri?,
    useAltTheme: Boolean,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("favorites", Context.MODE_PRIVATE) }
    val loading = remember { mutableStateOf(false) }

    LaunchedEffect(faceShape) {
        sharedViewModel.updateSelectedPrompt(faceShape)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient(useAltTheme))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.face_type_result, faceShape),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = stringResource(R.string.analyzed_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.recommended_styles_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false)
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

        PrimaryButton(
            text = stringResource(R.string.choose_style),
            onClick = {
                if (imageUri != null) {
                    loading.value = true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val imageFile = uriToFile(context, imageUri)
                            val maskBytes = enviarImagenParaMascara(imageFile)
                            if (maskBytes != null) {
                                val maskFile = File(context.filesDir, "mascara.png")
                                maskFile.writeBytes(maskBytes)
                                sharedViewModel.updateImageUri(imageUri)
                                navController.navigate("maskPreview")
                            } else {
                                Log.e("MASK", "❌ Error al obtener la máscara")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            loading.value = false
                        }
                    }
                }
            }
        )

        if (loading.value) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = stringResource(R.string.save_results),
            onClick = {
                val current = prefs.getStringSet("styles", emptySet())?.toMutableSet() ?: mutableSetOf()
                current.addAll(recommendedStyles)
                prefs.edit().putStringSet("styles", current).apply()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryButton(
            text = stringResource(R.string.go_back),
            onClick = { navController.popBackStack() }
        )
    }
}

suspend fun enviarImagenParaMascara(file: File): ByteArray? {
    return try {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("imagen", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("https://TU_NGROK/generar-mascara") // ⚠️ reemplaza con tu ngrok real
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.bytes()
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
