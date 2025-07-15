package com.example.capilux

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer {
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .build()

    private val detector: FaceDetector = FaceDetection.getClient(options)

    suspend fun analyzeFace(bitmap: Bitmap): FaceAnalysisResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val faces = Tasks.await(detector.process(image))
            if (faces.isEmpty()) {
                FaceAnalysisResult.Error("No se detectó ningún rostro")
            } else {
                analyzeFaceShape(faces) // Analizamos solo el primer rostro detectado
            }
        } catch (e: Exception) {
            FaceAnalysisResult.Error(e.message ?: "Error desconocido")
        }
    }

    private fun analyzeFaceShape(faces: List<Face>): FaceAnalysisResult {
        if (faces.isEmpty()) return FaceAnalysisResult.Error("No faces detected")

        val face = faces.first() // Analizamos el primer rostro detectado
        // Implementar lógica real de análisis aquí
        val boundingBox = face.boundingBox
        val width = boundingBox.width().toFloat()
        val height = boundingBox.height().toFloat()
        val ratio = width / height

        val shape = when {
            ratio > 1.1 -> "alargada"
            ratio < 0.9 -> "ovalada"
            else -> "redonda"
        }
        return FaceAnalysisResult.Success(shape, ratio)
    }
}

sealed class FaceAnalysisResult {
    data class Success(val faceShape: String, val ratio: Float) : FaceAnalysisResult()
    data class Error(val message: String) : FaceAnalysisResult()
}