package com.example.capilux.utils

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream

// 📦 Comprime la imagen para reducir resolución y tamaño
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

// 💾 Guarda una imagen en la galería del usuario
fun saveImageToGallery(context: Context, imageFile: File): Boolean {
    return try {
        val resolver = context.contentResolver
        val fileName = "Capilux_${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Capilux")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false

        resolver.openOutputStream(uri)?.use { output ->
            imageFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }

        true
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error guardando imagen: ${e.message}")
        false
    }
}

// 🗑️ Elimina un archivo de imagen local
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

// 📂 Convierte un Uri (content:// o file://) a un archivo físico temporal
fun uriToTempFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File(context.cacheDir, "captura_temp_${System.currentTimeMillis()}.jpg")
    inputStream?.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

// 🔒 Guarda una imagen (Uri) en el almacenamiento privado como "original_usuario.jpg"
fun saveImageToPrivateStorage(context: Context, sourceUri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(sourceUri)
        ?: throw IllegalArgumentException("No se pudo abrir el URI: $sourceUri")
    val destFile = File(context.filesDir, "original_usuario.jpg")
    inputStream.use { input ->
        destFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return destFile
}
