package com.example.capilux.utils

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Función para verificar si los permisos de la cámara están concedidos
fun isCameraPermissionGranted(activity: ComponentActivity): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Los permisos ya están concedidos en versiones anteriores a Marshmallow
    }
}

// Función para solicitar los permisos de cámara
fun requestCameraPermission(activity: ComponentActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
}

const val CAMERA_PERMISSION_REQUEST_CODE = 1001
