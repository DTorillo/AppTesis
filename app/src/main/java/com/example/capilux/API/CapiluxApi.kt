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

    private const val BASE_URL = "https://b327e847ace0.ngrok-free.app"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun procesarImagen(
        context: Context,
        imageUri: Uri,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d("Capilux", "👉 Iniciando procesarImagen")
            Log.d("Capilux", "📍 URI recibida: $imageUri")
            val imageFile = uriToFile(context, imageUri)
            Log.d("Capilux", "📁 File path: ${imageFile.absolutePath}")
            Log.d("Capilux", "📦 Existe imagen: ${imageFile.exists()}")

            if (!imageFile.exists()) {
                onError("Imagen no encontrada")
                return
            }

            // 1. Análisis de rostro
            Log.d("Capilux", "🔍 Analizando rostro...")
            val forma = analizarRostro(imageFile)
            Log.d("Capilux", "🔍 Forma obtenida: $forma")
            if (forma == null) {
                onError("No se pudo detectar forma del rostro")
                return
            }

            val prompt = obtenerPromptPorForma(forma)

            // 2. Generar máscara
            Log.d("Capilux", "🖤 Generando máscara...")
            val mascaraBytes = generarMascara(imageFile)
            Log.d("Capilux", "🖤 Bytes de máscara: ${mascaraBytes?.size}")
            if (mascaraBytes == null) {
                onError("Error generando máscara")
                return
            }

            val mascaraFile = File(context.cacheDir, "mascara_tmp.png")
            mascaraFile.writeBytes(mascaraBytes)

            // 3. Generar estilo
            Log.d("Capilux", "🎨 Aplicando estilo...")
            val resultado = aplicarEstilo(imageFile, mascaraFile, prompt)
            Log.d("Capilux", "🎨 Resultado recibido: ${resultado?.size}")
            if (resultado != null) {
                onSuccess(resultado)
            } else {
                onError("Fallo en generación final")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Capilux", "❌ Error general: ${e.message}")
            onError("Error general: ${e.message}")
        }
    }

    private suspend fun analizarRostro(imagen: File): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("Capilux", "🌐 Enviando a: $BASE_URL/rostro/analizar")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("imagen", imagen.name, imagen.asRequestBody("image/jpeg".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/rostro/analizar")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("Capilux", "🌐 Código HTTP: ${response.code}")
                val body = response.body?.string()
                Log.d("Capilux", "🌐 Body recibido: $body")
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(body ?: return@withContext null)
                val texto = json.optString("resultado", "")
                val lineaForma = texto.lines().find { it.contains("Forma del rostro:") }
                return@withContext lineaForma?.split(":")?.getOrNull(1)?.trim()?.lowercase()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Capilux", "❌ Error en analizarRostro: ${e.message}")
            return@withContext null
        }
    }

    private suspend fun generarMascara(imagen: File): ByteArray? = withContext(Dispatchers.IO) {
        try {
            Log.d("Capilux", "🌐 Enviando a: $BASE_URL/mascara/generar-mascara")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("imagen", imagen.name, imagen.asRequestBody("image/jpeg".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/mascara/generar-mascara")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("Capilux", "🌐 Código HTTP máscara: ${response.code}")
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    Log.d("Capilux", "🌐 Bytes recibidos de máscara: ${bytes?.size}")
                    return@withContext bytes
                } else {
                    Log.d("Capilux", "🌐 Error generando máscara: ${response.body?.string()}")
                    return@withContext null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Capilux", "❌ Error en generarMascara: ${e.message}")
            return@withContext null
        }
    }

    private suspend fun aplicarEstilo(imagen: File, mascara: File, prompt: String): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                Log.d("Capilux", "🌐 Enviando a: $BASE_URL/estilo/generar")
                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("imagen", imagen.name, imagen.asRequestBody("image/jpeg".toMediaType()))
                    .addFormDataPart("mascara", mascara.name, mascara.asRequestBody("image/png".toMediaType()))
                    .addFormDataPart("prompt", prompt)
                    .build()

                val request = Request.Builder()
                    .url("$BASE_URL/estilo/generar")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d("Capilux", "🌐 Código HTTP estilo: ${response.code}")
                    if (response.isSuccessful) {
                        val bytes = response.body?.bytes()
                        Log.d("Capilux", "🌐 Bytes recibidos estilo: ${bytes?.size}")
                        return@withContext bytes
                    } else {
                        Log.d("Capilux", "🌐 Error generando estilo: ${response.body?.string()}")
                        return@withContext null
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Capilux", "❌ Error en aplicarEstilo: ${e.message}")
                return@withContext null
            }
        }

    private fun obtenerPromptPorForma(forma: String): String {
        return when (forma.lowercase()) {
            "ovalado"    -> "modern volumized haircut, clean sides"
            "cuadrado"   -> "crew cut, masculine clean style"
            "redondo"    -> "modern pompadour, stylish top volume"
            "triangular" -> "textured quiff haircut, balanced top"
            "alargado"   -> "medium haircut with bangs"
            else         -> "short stylish haircut"
        }
    }

    fun uriToFile(context: Context, uri: Uri): File {
        Log.d("Capilux", "📂 Copiando URI a archivo temporal...")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el URI: $uri")

        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        Log.d("Capilux", "📂 Archivo temporal creado: ${tempFile.absolutePath}")
        return tempFile
    }
}
