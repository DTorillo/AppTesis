package com.example.capilux.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeStyle { DEFAULT, GRADIENT, METALLIC }

val GradientColorScheme: ColorScheme = lightColorScheme(
    primary = GradientPurple,
    secondary = GradientGrey,
    tertiary = GradientBlue
)

val MetallicColorScheme: ColorScheme = darkColorScheme(
    primary = MetallicGrey,
    secondary = MetallicBlack,
    tertiary = Color(0xFF757575)
)

