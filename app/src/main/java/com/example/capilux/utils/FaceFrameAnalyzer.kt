package com.example.capilux.utils

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Analizador que detecta si hay un rostro en el cuadro.
 * Llama a [onResult] con true cuando se detecta al menos un rostro.
 */
class FaceFrameAnalyzer(private val onResult: (Boolean) -> Unit) : ImageAnalysis.Analyzer {
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    val width = image.width
                    val height = image.height
                    val centerRegion = Rect(
                        (width * 0.25f).toInt(),
                        (height * 0.2f).toInt(),
                        (width * 0.75f).toInt(),
                        (height * 0.8f).toInt()
                    )
                    val inside = faces.any { face ->
                        centerRegion.contains(face.boundingBox.centerX(), face.boundingBox.centerY())
                    }
                    onResult(inside)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
            onResult(false)
        }
    }
}
