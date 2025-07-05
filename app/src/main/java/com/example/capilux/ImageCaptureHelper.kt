package com.example.capilux

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

fun captureImage(
    context: Context,
    controller: LifecycleCameraController,
    executor: Executor,
    onSuccess: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    // Crear directorio si no existe
    val outputDir = File(context.getExternalFilesDir(null), "Capilux")
    if (!outputDir.exists()) outputDir.mkdirs()

    // Crear archivo con timestamp
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val photoFile = File(outputDir, "CAPILUX_$timestamp.jpg")

    // Configuración de la captura
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onSuccess(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                onError(exc)
            }
        }
    )
}