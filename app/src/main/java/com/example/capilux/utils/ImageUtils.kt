package com.example.capilux.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
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
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return false
        val fileName = "CAPILUX_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Capilux"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let { outUri ->
            resolver.openOutputStream(outUri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(outUri, values, null, null)
            true
        } ?: false
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error al guardar imagen: ${e.message}")
        false
    }
}