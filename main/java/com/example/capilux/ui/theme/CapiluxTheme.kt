package com.example.capilux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun CapiluxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeStyle: ThemeStyle = ThemeStyle.DEFAULT,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val colorScheme = when(themeStyle) {
        ThemeStyle.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
        ThemeStyle.GRADIENT -> GradientColorScheme
        ThemeStyle.METALLIC -> MetallicColorScheme
    }

    LaunchedEffect(darkTheme, themeStyle) {
        val barColor = when(themeStyle) {
            ThemeStyle.GRADIENT -> GradientPurple
            ThemeStyle.METALLIC -> Color.Black
            ThemeStyle.DEFAULT -> if (darkTheme) Color.Black else Color.White
        }
        systemUiController.setSystemBarsColor(
            color = barColor,
            darkIcons = themeStyle == ThemeStyle.DEFAULT && !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)