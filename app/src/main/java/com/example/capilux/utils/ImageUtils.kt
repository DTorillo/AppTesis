package com.example.capilux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

fun compressImage(context: Context, originalUri: Uri): Uri {
    return try {
        val outputDir = context.cacheDir
        val compressedFile = File(outputDir, "compressed_${System.currentTimeMillis()}.jpg")

        val options = BitmapFactory.Options().apply {
            inSampleSize = 2 // Reducción de resolución
        }

        context.contentResolver.openInputStream(originalUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)?.let { bitmap ->
                FileOutputStream(compressedFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                }
                Uri.fromFile(compressedFile)
            } ?: originalUri
        } ?: originalUri
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error comprimiendo imagen: ${e.message}")
        originalUri
    }
}