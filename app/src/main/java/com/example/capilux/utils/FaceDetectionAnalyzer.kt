package com.example.capilux.utils

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectionAnalyzer(
    private val onResult: (Boolean) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(options)

    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val centerX = inputImage.width / 2
                    val centerY = inputImage.height / 2
                    val inCenter = faces.firstOrNull()?.boundingBox?.contains(centerX, centerY) ?: false
                    onResult(inCenter)
                    image.close()
                }
                .addOnFailureListener {
                    onResult(false)
                    image.close()
                }
        } else {
            onResult(false)
            image.close()
        }
    }
}
