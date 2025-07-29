package com.example.capilux.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

private interface GenerationService {
    @Multipart
    @POST("analizar")
    suspend fun generar(
        @Part imagen: MultipartBody.Part,
        @Part("prompt") prompt: okhttp3.RequestBody
    ): Response<ResponseBody>
}

object GenerationApi {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private val service: GenerationService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(GenerationService::class.java)
    }

    suspend fun enviarImagenConPrompt(file: File, prompt: String): ByteArray? {
        return try {
            val imagePart = MultipartBody.Part.createFormData(
                "imagen",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
            val promptPart = prompt.toRequestBody("text/plain".toMediaType())
            val response = service.generar(imagePart, promptPart)
            if (response.isSuccessful) {
                response.body()?.bytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
