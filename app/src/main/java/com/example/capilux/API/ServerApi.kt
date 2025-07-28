package com.example.capilux.network

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object ServerApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val SERVER_URL = "https://e4fdfa4a67af.ngrok-free.app/analizar"

    fun enviarImagen(
        context: Context,
        imageUri: Uri,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val file = File(imageUri.path ?: "")
            if (!file.exists()) {
                onError("No se encontró el archivo de imagen.")
                return
            }

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                    "imagen",
                    file.name,
                    file.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(SERVER_URL)
                .post(requestBody)
                .build()

            val startTime = System.currentTimeMillis()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    Log.e("ServerApi", "❌ Conexión fallida en ${duration}ms: ${e.message}")
                    onError("No se pudo conectar con el servidor. Tiempo: ${duration}ms")
                }

                override fun onResponse(call: Call, response: Response) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    Log.d("ServerApi", "✅ Respuesta recibida en ${duration}ms")

                    if (!response.isSuccessful) {
                        onError("Respuesta inválida del servidor. Tiempo: ${duration}ms")
                        return
                    }

                    val body = response.body?.string()
                    if (body != null) {
                        Log.d("ServerApi", "Respuesta recibida: $body")
                        onResult(body)
                    } else {
                        onError("Respuesta vacía del servidor. Tiempo: ${duration}ms")
                    }
                }
            })

        } catch (e: Exception) {
            onError("Error interno: ${e.message}")
        }
    }
}