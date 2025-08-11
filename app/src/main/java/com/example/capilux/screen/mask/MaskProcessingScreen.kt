// MaskProcessingScreen.kt
package com.example.capilux.screen.mask

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.capilux.network.CapiluxApi
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaskProcessingScreen(
    imageUri: String,
    useAltTheme: Boolean,
    navController: NavHostController
) {
    val context = LocalContext.current
    val gradient = backgroundGradient(useAltTheme)
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val originalFile = File(context.filesDir, "original_usuario.jpg")
    val decodedUri = Uri.fromFile(originalFile)

    // Animación de carga mejorada
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(originalFile.absolutePath) {
        if (!originalFile.exists() || originalFile.length() < 10_000) {
            scope.launch(Dispatchers.Main) {
                error.value = context.getString(R.string.mask_processing_original_missing)
                loading.value = false
            }
            return@LaunchedEffect
        }

        CapiluxApi.generarMascara(
            context = context,
            imageUri = decodedUri,
            onSuccess = { bytes ->
                scope.launch(Dispatchers.Main) {
                    try {
                        val file = File(context.filesDir, "mascara_tmp.png")
                        file.writeBytes(bytes)
                        navController.navigate("maskPreviewScreen/${Uri.encode(originalFile.absolutePath)}") {
                            popUpTo("maskProcessingScreen/$imageUri") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        error.value = context.getString(R.string.mask_processing_save_error, e.message)
                    }
                    loading.value = false
                }
            },
            onError = { mensaje ->
                scope.launch(Dispatchers.Main) {
                    error.value = mensaje
                    loading.value = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mask_processing_title),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        enabled = !loading.value
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading.value -> {
                    AppLoadingAnimation(
                        rotation = rotation,
                        pulseAlpha = pulseAlpha,
                        message = stringResource(R.string.mask_processing_transforming),
                        subMessage = stringResource(R.string.mask_processing_submessage),
                        useAltTheme = useAltTheme
                    )
                }
                error.value != null -> {
                    AppErrorMessage(
                        message = error.value!!,
                        onRetry = {
                            loading.value = true
                            error.value = null
                            scope.launch {
                                if (originalFile.exists() && originalFile.length() >= 10_000) {
                                    CapiluxApi.generarMascara(
                                        context = context,
                                        imageUri = decodedUri,
                                        onSuccess = { bytes ->
                                            scope.launch(Dispatchers.Main) {
                                                try {
                                                    val file = File(context.filesDir, "mascara_tmp.png")
                                                    file.writeBytes(bytes)
                                                    navController.navigate("maskPreviewScreen/${Uri.encode(originalFile.absolutePath)}") {
                                                        popUpTo("maskProcessingScreen/$imageUri") { inclusive = true }
                                                    }
                                                } catch (e: Exception) {
                                                    error.value = context.getString(R.string.mask_processing_save_error_generic, e.message)
                                                }
                                                loading.value = false
                                            }
                                        },
                                        onError = { mensaje ->
                                            scope.launch(Dispatchers.Main) {
                                                error.value = mensaje
                                                loading.value = false
                                            }
                                        }
                                    )
                                } else {
                                    error.value = context.getString(R.string.mask_processing_invalid_image)
                                    loading.value = false
                                }
                            }
                        },
                        onBack = { navController.popBackStack() },
                        useAltTheme = useAltTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLoadingAnimation(
    rotation: Float,
    pulseAlpha: Float,
    message: String,
    subMessage: String,
    useAltTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (useAltTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val onPrimaryColor = if (useAltTheme) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
    val secondaryColor = if (useAltTheme) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Círculo de carga mejorado con gradiente y efecto de pulso
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = primaryColor.copy(alpha = 0.3f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            // Anillo exterior animado
            CircularProgressIndicator(
                modifier = Modifier
                    .size(140.dp)
                    .rotate(rotation),
                strokeWidth = 8.dp,
                color = secondaryColor.copy(alpha = pulseAlpha),
                trackColor = Color.Transparent
            )

            // Círculo central con icono
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = pulseAlpha * 0.8f))
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = onPrimaryColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation * 0.75f)
                )
            }
        }

        // Contenido textual mejorado
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = pulseAlpha),
                textAlign = TextAlign.Center
            )

            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Puntos de carga animados
        LoadingDots(useAltTheme = useAltTheme)
    }
}

@Composable
private fun LoadingDots(useAltTheme: Boolean) {
    val dotColor = if (useAltTheme)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun AnimatedDot(delay: Int) {
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.3f at delay
                    1f at delay + 300
                    0.3f at delay + 600
                },
                repeatMode = RepeatMode.Restart
            )
        )

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = alpha))
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedDot(0)
        AnimatedDot(150)
        AnimatedDot(300)
    }
}

@Composable
private fun AppErrorMessage(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    useAltTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val errorContainerColor = if (useAltTheme)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val onErrorContainerColor = if (useAltTheme)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .padding(24.dp)
            .width(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = errorContainerColor.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = stringResource(R.string.error),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.mask_processing_error_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = onErrorContainerColor,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = onErrorContainerColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = onErrorContainerColor
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.go_back))
                }

                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (useAltTheme)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.primary,
                        contentColor = if (useAltTheme)
                            MaterialTheme.colorScheme.onSecondary
                        else
                            MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}