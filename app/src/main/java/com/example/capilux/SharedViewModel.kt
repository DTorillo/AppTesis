package com.example.capilux

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    var imageUri: Uri? by mutableStateOf(null)
        private set

    var selectedPrompt: String? by mutableStateOf(null)
        private set

    fun updateImageUri(uri: Uri?) {
        imageUri = uri
    }

    fun updateSelectedPrompt(prompt: String?) {
        selectedPrompt = prompt
    }

    fun clear() {
        imageUri = null
        selectedPrompt = null
    }
}
