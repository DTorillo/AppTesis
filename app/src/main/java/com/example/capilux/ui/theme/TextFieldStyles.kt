package com.example.capilux.ui.theme

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun gradientTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedContainerColor = Color.White.copy(alpha = 0.15f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        focusedIndicatorColor = Color.White,
        unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
        cursorColor = Color.White
    )
}
