package com.example.capilux.network

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

object ServerApi {
    private val client = OkHttpClient()
    private const val SERVER_URL = "http://192.168.100.106:5000/analizar" // IP de tu PC

    fun enviarImagen(context: Context, imageUri: Uri, onResult: (String) -> Unit, onError: (String) -> Unit) {
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

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ServerApi", "Error en conexión: ${e.message}")
                    onError("No se pudo conectar con el servidor.")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        onError("Respuesta inválida del servidor.")
                        return
                    }

                    val body = response.body?.string()
                    if (body != null) {
                        onResult(body)
                    } else {
                        onError("Respuesta vacía del servidor.")
                    }
                }
            })

        } catch (e: Exception) {
            onError("Error interno: ${e.message}")
        }
    }
}
