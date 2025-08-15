package com.example.capilux

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.max

data class GuidanceStatus(
    val centered: Boolean,
    val distanceOk: Boolean,
    val anglesOk: Boolean,
    val lightingOk: Boolean,
    val sharpnessOk: Boolean,
    val ready: Boolean,
    val message: String
)

private fun buildDetector(): FaceDetector {
    val opts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    return FaceDetection.getClient(opts)
}

fun configureController(controller: LifecycleCameraController) {
    controller.setEnabledUseCases(
        CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
    )
    controller.imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
    controller.setImageCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
}
@OptIn(ExperimentalGetImage::class)
fun startGuidedAnalysis(
    context: Context,
    controller: LifecycleCameraController,
    executor: Executor,
    autoCapture: Boolean = true,
    stableFramesNeeded: Int = 8,
    onGuidance: (GuidanceStatus) -> Unit,
    onAutoCaptureSuccess: (Uri) -> Unit,
    onAutoCaptureError: (Exception) -> Unit
) {
    val detector = buildDetector() // ✅ reusar el mismo detector
    var processing = false
    var stableCounter = 0

    controller.setImageAnalysisAnalyzer(executor) { imageProxy: ImageProxy ->
        if (processing) {
            imageProxy.close()
            return@setImageAnalysisAnalyzer
        }
        processing = true

        try {
            val mediaImage = imageProxy.image ?: return@setImageAnalysisAnalyzer
            val rotation = imageProxy.imageInfo.rotationDegrees
            val inImage = InputImage.fromMediaImage(mediaImage, rotation)

            // ✅ reusar detector (no crear uno nuevo por frame)
            val faces = Tasks.await(detector.process(inImage))
            val status = evaluateGuidance(faces, imageProxy)
            onGuidance(status)

            if (autoCapture && status.ready) {
                stableCounter++
                if (stableCounter >= stableFramesNeeded) {
                    captureImage(
                        context,
                        controller,
                        executor,
                        { uri -> onAutoCaptureSuccess(uri) },
                        { e -> onAutoCaptureError(e) }
                    )
                    stableCounter = 0
                }
            } else {
                stableCounter = 0
            }
        } catch (_: Exception) {
            // Ignorar errores de frame sueltos
        } finally {
            processing = false
            imageProxy.close()
        }
    }
}

/**
 * Criterios de guía:
 * - 1 rostro
 * - Tamaño: bbox >= 35% del ancho y 45% del alto de la vista
 * - Centrado: desvío <= 10% del centro
 * - Ángulos: |yaw|<=12°, |roll|<=10°
 * - Luz: 60<= media Y <=190
 * - Nitidez: var de diferencias Y >= 18.0 (ajústalo por dispositivo)
 */
private fun evaluateGuidance(faces: List<Face>, proxy: ImageProxy): GuidanceStatus {
    if (faces.isEmpty()) {
        return GuidanceStatus(false, false, false, false, false, false, "Pon tu rostro dentro del marco")
    }
    if (faces.size > 1) {
        return GuidanceStatus(false, false, false, false, false, false, "Solo una persona en el encuadre")
    }

    val face = faces.first()
    val w = proxy.width.toFloat()
    val h = proxy.height.toFloat()
    val bbox = face.boundingBox

    val sizeOk = (bbox.width() / w >= 0.35f) && (bbox.height() / h >= 0.45f)
    val cx = bbox.exactCenterX() / w
    val cy = bbox.exactCenterY() / h
    val centered = (abs(cx - 0.5f) <= 0.10f) && (abs(cy - 0.5f) <= 0.10f)

    val yaw = face.headEulerAngleY
    val roll = face.headEulerAngleZ
    val anglesOk = (kotlin.math.abs(yaw) <= 12f) && (kotlin.math.abs(roll) <= 10f)

    val (lightingOk, sharpnessOk, msgLight, msgSharp) = evaluateLumaAndSharpness(proxy)

    val ready = centered && sizeOk && anglesOk && lightingOk && sharpnessOk
    val message = when {
        !sizeOk -> "Acércate un poco al teléfono"
        !centered -> "Centra el rostro en el marco"
        !anglesOk -> "Mira de frente y endereza la cabeza"
        !lightingOk -> msgLight
        !sharpnessOk -> msgSharp
        else -> "¡Perfecto! Mantente quieto…"
    }
    return GuidanceStatus(centered, sizeOk, anglesOk, lightingOk, sharpnessOk, ready, message)
}

/**
 * Evalúa iluminación y nitidez en plano Y (luma).
 */
private fun evaluateLumaAndSharpness(proxy: ImageProxy): Quad<Boolean, Boolean, String, String> {
    val planeY = proxy.planes[0].buffer
    val rowStride = proxy.planes[0].rowStride
    val pixelStride = proxy.planes[0].pixelStride
    val width = proxy.width
    val height = proxy.height
    val data = ByteArray(planeY.remaining())
    planeY.get(data)

    var sum = 0.0
    var count = 0
    for (y in 0 until height step 2) {
        var offset = y * rowStride
        for (x in 0 until width step 2) {
            val v = data[offset].toInt() and 0xFF
            sum += v
            count++
            offset += pixelStride * 2
        }
    }
    val mean = sum / max(1, count)

    var diffSum = 0.0
    var diffCount = 0
    for (y in 0 until height step 2) {
        var offset = y * rowStride
        var prev = -1
        for (x in 0 until width step 4) {
            val v = data[offset].toInt() and 0xFF
            if (prev >= 0) {
                val d = (v - prev)
                diffSum += d * d
                diffCount++
            }
            prev = v
            offset += pixelStride * 2
        }
    }
    val sharpnessMetric = diffSum / max(1, diffCount)
    val lightingOk = mean in 60.0..190.0
    val sharpnessOk = sharpnessMetric >= 18.0

    val msgLight = if (!lightingOk) {
        if (mean < 60) "Mejor ilumina tu rostro (muy oscuro)" else "Baja la luz frontal (muy brillante)"
    } else "OK"
    val msgSharp = if (!sharpnessOk) "Evita movimiento: mantén la cámara firme" else "OK"

    return Quad(lightingOk, sharpnessOk, msgLight, msgSharp)
}

data class Quad<A,B,C,D>(val first: A, val second: B, val third: C, val fourth: D)

fun captureImage(
    context: Context,
    controller: LifecycleCameraController,
    executor: Executor,
    onSuccess: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val outputDir = File(context.getExternalFilesDir(null), "Capilux").apply { mkdirs() }
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
    val photoFile = File(outputDir, "CAPILUX_$timestamp.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSuccess(Uri.fromFile(photoFile))
            }
            override fun onError(exc: ImageCaptureException) {
                onError(exc)
            }
        }
    )
}
