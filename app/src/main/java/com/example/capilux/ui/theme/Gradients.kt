package com.example.capilux.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

@Composable
fun backgroundGradient(useAltTheme: Boolean): Brush {
    return Brush.verticalGradient(
        colors = if (useAltTheme) {
            listOf(GradientPurple, GradientGray)
        } else {
            listOf(GradientPurple, GradientBlue)
        }
    )
}

