package com.example.capilux

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var faceShape: String by mutableStateOf("")
        private set
    var imageUri: Uri? by mutableStateOf(null)
        private set
    var analysisResult: String by mutableStateOf("")
        private set

    // Nombre visible del estilo (para mostrar en UI)
    var selectedPrompt: String? by mutableStateOf(null)
        private set

    // Prompt técnico realmente enviado al backend (para regenerar idéntico)
    var selectedPromptText: String? by mutableStateOf(null)
        private set

    fun updateImageUri(uri: Uri?) { imageUri = uri }
    fun updateFaceShape(shape: String) { faceShape = shape }
    fun updateAnalysisResult(result: String) { analysisResult = result }
    fun updateSelectedPrompt(prompt: String?) { selectedPrompt = prompt }
    fun updateSelectedPromptText(promptText: String?) { selectedPromptText = promptText }

    // 🔥 Métodos para limpiar (nunca llames clear() del ViewModel base)
    fun clearAll() {
        faceShape = ""
        imageUri = null
        analysisResult = ""
        selectedPrompt = null
        selectedPromptText = null
    }

    fun clearSelectedPrompt() {
        selectedPrompt = null
        selectedPromptText = null
    }
    fun clearImageData() { imageUri = null }
    fun clearFaceShape() { faceShape = "" }
    fun clearAnalysisResult() { analysisResult = "" }
}
