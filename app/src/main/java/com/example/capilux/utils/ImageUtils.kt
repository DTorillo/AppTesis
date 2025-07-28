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

fun saveImageToGallery(context: Context, imageUri: Uri): Boolean {
    return try {
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(imageUri) ?: return false
        val fileName = "Capilux_${System.currentTimeMillis()}.jpg"
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
        }
        val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false
        resolver.openOutputStream(uri)?.use { output ->
            inputStream.copyTo(output)
        }
        true
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error guardando imagen: ${e.message}")
        false
    }
}

fun deleteImageFile(context: Context, uriString: String): Boolean {
    return try {
        val uri = Uri.parse(uriString)
        val file = File(uri.path ?: return false)
        file.delete()
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error eliminando imagen: ${e.message}")
        false
    }
}