package com.example.capilux

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var imageUri by mutableStateOf<Uri?>(null)
        private set

    var selectedPrompt by mutableStateOf<String?>(null)
        private set

    fun setImageUri(uri: Uri?) {
        imageUri = uri
    }

    fun setSelectedPrompt(prompt: String) {
        selectedPrompt = prompt
    }

    fun clear() {
        imageUri = null
        selectedPrompt = null
    }
}
