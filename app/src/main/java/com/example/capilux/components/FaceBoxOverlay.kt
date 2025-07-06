package com.example.capilux.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun FaceBoxOverlay(faceAligned: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // Marco donde el usuario debe colocar su rostro
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(220.dp)
                .border(2.dp, Color.White, RoundedCornerShape(8.dp))
        )
        // Mensaje de ayuda
        Text(
            text = if (faceAligned) "Puedes tomar la foto" else "Coloca tu rostro en el marco",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color(0xAA2D0C5A), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
