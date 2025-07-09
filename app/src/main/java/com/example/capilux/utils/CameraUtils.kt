package com.example.capilux.utils

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtils {
    fun getOutputDirectory(context: Context): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, "Capilux").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    fun createTimestampedFile(baseFolder: File, prefix: String, extension: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return File(baseFolder, "${prefix}${timestamp}${extension}")
    }
}
fun takePhoto(
    cameraController: LifecycleCameraController,
    context: Context,
    onSuccess: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val outputDir = CameraUtils.getOutputDirectory(context)
    val photoFile = CameraUtils.createTimestampedFile(outputDir, "CAPILUX_", ".jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // 🔥 Activar máxima calidad
    cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                    .putString("last_captured_image", savedUri.toString()).apply()
                onSuccess(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                onError("Error al capturar la foto: ${exc.message}")
            }
        }
    )
}

