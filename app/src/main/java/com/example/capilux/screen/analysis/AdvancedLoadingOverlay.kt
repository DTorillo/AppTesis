package com.example.capilux.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capilux.R
import com.example.capilux.ui.theme.backgroundGradient

@Composable
fun AdvancedLoadingOverlay(
    message: String,
    subMessage: String? = null,
    useAltTheme: Boolean,
    logo: Painter = painterResource(id = R.drawable.logo)
) {
    val gradient = backgroundGradient(useAltTheme)
    val progressColor = if (useAltTheme) Color(0xFF6A35FF) else Color(0xFF593A8D)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animación de pulso
            PulsingLogo(logo = logo)

            // Indicador de progreso circular
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = progressColor,
                strokeWidth = 6.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mensaje principal
            Text(
                text = message,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Submensaje opcional
            subMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animación de puntos
            DotAnimation(progressColor = progressColor)
        }
    }
}

@Composable
private fun PulsingLogo(logo: Painter) {
    // Animación de pulso (crece y disminuye suavemente)
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0 with LinearEasing
                1.1f at 750 with FastOutSlowInEasing
                1.0f at 1500 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAnimation"
    )

    // Sombra que se sincroniza con el pulso (solución con animateFloat)
    val shadowElevationFloat by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                8f at 0 with LinearEasing
                16f at 750 with FastOutSlowInEasing
                8f at 1500 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shadowAnimation"
    )
    val shadowElevation = shadowElevationFloat.dp

    Image(
        painter = logo,
        contentDescription = "Logo con efecto de pulso",
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 32.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
            .shadow(
                elevation = shadowElevation,
                shape = CircleShape,
                spotColor = Color(0xFF6A35FF).copy(alpha = 0.3f)
            ),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun DotAnimation(progressColor: Color) {
    val isAnimating = remember { true }

    Row(
        modifier = Modifier.height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            DotLoader(
                index = index,
                color = progressColor,
                isAnimating = isAnimating
            )
        }
    }
}

@Composable
private fun DotLoader(index: Int, color: Color, isAnimating: Boolean) {
    val delay = index * 200L
    val animatedSize by animateDpAsState(
        targetValue = if (isAnimating) 12.dp else 8.dp,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAnimation"
    )

    Box(
        modifier = Modifier
            .size(animatedSize)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.7f))
    )
}