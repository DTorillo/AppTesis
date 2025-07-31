package com.example.capilux.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object CapiluxApi {

    private const val BASE_URL = "https://7198e702ea4a.ngrok-free.app"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // --- 1. Análisis de simetría/tipo de rostro ---
    suspend fun analizarSimetria(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val imageFile = uriToTempFile(context, imageUri)
                if (!imageFile.exists()) {
                    onError("Imagen no encontrada")
                    return@withContext
                }
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("imagen", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/rostro/analizar")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        onError("Error de análisis facial: ${response.code}")
                        return@use
                    }
                    val body = response.body?.string() ?: ""
                    onSuccess(body)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error en análisis facial: ${e.message}")
            }
        }
    }

    // --- 2. Generación de máscara (cabello) ---
    suspend fun generarMascara(
        context: Context,
        imageUri: Uri,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val imageFile = uriToTempFile(context, imageUri)
                if (!imageFile.exists()) {
                    onError("Imagen no encontrada para máscara")
                    return@withContext
                }
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("imagen", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/mascara/generar-mascara")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        onError("Error generando máscara: ${response.code}")
                        return@use
                    }
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        onSuccess(bytes)
                    } else {
                        onError("Respuesta de máscara vacía")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error en generación de máscara: ${e.message}")
            }
        }
    }

    // --- 3. Generación de corte/estilo (IA generativa) ---
    suspend fun generarEstilo(
        context: Context,
        imageUri: Uri,
        mascaraFile: File,
        prompt: String,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val imageFile = uriToTempFile(context, imageUri)
                if (!imageFile.exists() || !mascaraFile.exists()) {
                    onError("Imagen o máscara no encontrada para IA generativa")
                    return@withContext
                }
                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("imagen", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType()))
                    .addFormDataPart("mascara", mascaraFile.name, mascaraFile.asRequestBody("image/png".toMediaType()))
                    .addFormDataPart("prompt", prompt)
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/estilo/generar")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        onError("Error generando estilo: ${response.code}")
                        return@use
                    }
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        onSuccess(bytes)
                    } else {
                        onError("Respuesta de IA generativa vacía")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error en generación de estilo: ${e.message}")
            }
        }
    }

    // Utilidad: convertir Uri a archivo temporal seguro
    fun uriToTempFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el URI: $uri")
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_${(0..10000).random()}.jpg")
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
