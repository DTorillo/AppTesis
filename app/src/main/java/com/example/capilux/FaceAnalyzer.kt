package com.example.capilux

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class FaceAnalyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // ojos abiertos, sonrisa (opcional guía)
        .enableTracking()
        .build()

    private val detector: FaceDetector = FaceDetection.getClient(options)

    suspend fun analyzeFace(bitmap: Bitmap): FaceAnalysisResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            val faces = Tasks.await(detector.process(image))
            if (faces.isEmpty()) {
                FaceAnalysisResult.Error("No se detectó ningún rostro")
            } else {
                analyzeFaceShape(faces.first(), bitmap.width.toFloat(), bitmap.height.toFloat())
            }
        } catch (e: Exception) {
            FaceAnalysisResult.Error(e.message ?: "Error desconocido")
        }
    }

    // ======= UTIL =======
    private fun d(a: PointF, b: PointF) = hypot(a.x - b.x, a.y - b.y)

    private fun getPoint(contours: Map<Int, FaceContour>, type: Int, index: Int): PointF? {
        return contours[type]?.points?.getOrNull(index)
    }

    // Convex hull simple -> Rect ajustado
    private fun preciseFrameRect(face: Face): RectF {
        val pts = face.allContours.flatMap { it.points }
        var minX = Float.POSITIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY
        for (p in pts) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y
            if (p.x > maxX) maxX = p.x
            if (p.y > maxY) maxY = p.y
        }
        // Margen muy pequeño (2%) para no cortar barba/pelo frontal
        val w = maxX - minX
        val h = maxY - minY
        val mx = 0.02f * w
        val my = 0.02f * h
        return RectF(minX - mx, minY - my, maxX + mx, maxY + my)
    }

    private fun analyzeFaceShape(face: Face, imgW: Float, imgH: Float): FaceAnalysisResult {
        val contours = face.allContours.associateBy { it.faceContourType }

        // === Puntos “análogos” a tu Python ===
        // Mejillas: usar extremos del contorno de cara
        val faceContour = contours[FaceContour.FACE]?.points ?: emptyList()
        if (faceContour.isEmpty()) return FaceAnalysisResult.Error("Sin contorno facial")

        val leftMost  = faceContour.minByOrNull { it.x }!!
        val rightMost = faceContour.maxByOrNull { it.x }!!
        val chin = contours[FaceContour.FACE]?.points?.maxByOrNull { it.y }
            ?: faceContour.maxByOrNull { it.y }!!

        // “Entre-ceja” aprox: punto medio entre cejas (usar 2º-3º puntos de cada ceja top)
        val lBrow = contours[FaceContour.LEFT_EYEBROW_TOP]?.points
        val rBrow = contours[FaceContour.RIGHT_EYEBROW_TOP]?.points
        if (lBrow.isNullOrEmpty() || rBrow.isNullOrEmpty()) {
            return FaceAnalysisResult.Error("No se obtuvieron cejas")
        }
        val browCenter = PointF(
            (lBrow.getOrNull(2)?.x ?: lBrow.first().x + rBrow.first().x) / 2f,
            (lBrow.getOrNull(2)?.y ?: lBrow.first().y + rBrow.first().y) / 2f
        )

        // Mandíbula: aprox usando extremos del contorno FACE
        val jawLeft = leftMost
        val jawRight = rightMost

        // Frente: extremos superiores de cejas como proxy del ancho frontal
        val foreheadLeft = lBrow.first()
        val foreheadRight = rBrow.last()

        // Labios
        val lipTop = getPoint(contours, FaceContour.UPPER_LIP_TOP, 5)
            ?: contours[FaceContour.UPPER_LIP_TOP]?.points?.getOrNull(
                (contours[FaceContour.UPPER_LIP_TOP]?.points?.size ?: 1) / 2
            )
        val lipBottom = getPoint(contours, FaceContour.LOWER_LIP_BOTTOM, 5)
            ?: contours[FaceContour.LOWER_LIP_BOTTOM]?.points?.getOrNull(
                (contours[FaceContour.LOWER_LIP_BOTTOM]?.points?.size ?: 1) / 2
            )

        if (lipTop == null || lipBottom == null) {
            return FaceAnalysisResult.Error("No se obtuvieron labios")
        }

        // === Medidas (px) ===
        val ancho = d(leftMost, rightMost)
        val alto = d(chin, browCenter)
        val mandibula = d(jawLeft, jawRight)
        val frente = d(foreheadLeft, foreheadRight)
        val labioMenton = d(chin, lipBottom)
        val labiosGap = d(lipTop, lipBottom)

        // === Ratios (idénticos a tu Python) ===
        val rAltoAncho = alto / max(ancho, 1e-6f)
        val rFrenteMandibula = frente / max(mandibula, 1e-6f)
        val rMentonAlto = labioMenton / max(alto, 1e-6f)

        val shape = when {
            rAltoAncho >= 1.6f -> "alargado"
            rAltoAncho <= 1.1f && rFrenteMandibula < 1.1f -> "redondo"
            rAltoAncho <= 1.1f && rFrenteMandibula >= 1.1f -> "cuadrado"
            rFrenteMandibula > 1.25f && rMentonAlto < 0.2f -> "corazon"
            else -> "ovalado"
        }

        val preciseRect = preciseFrameRect(face)

        return FaceAnalysisResult.Success(
            faceShape = shape,
            ratio = rAltoAncho,
            preciseFrame = preciseRect,
            measuresPx = MeasuresPx(
                ancho, alto, mandibula, frente, labioMenton, labiosGap
            ),
            imageSize = Pair(imgW, imgH)
        )
    }
}

data class MeasuresPx(
    val ancho: Float,
    val alto: Float,
    val mandibula: Float,
    val frente: Float,
    val labioMenton: Float,
    val labiosGap: Float
)

sealed class FaceAnalysisResult {
    data class Success(
        val faceShape: String,
        val ratio: Float,
        val preciseFrame: RectF,
        val measuresPx: MeasuresPx,
        val imageSize: Pair<Float, Float>
    ) : FaceAnalysisResult()

    data class Error(val message: String) : FaceAnalysisResult()
}
