package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.R
import com.example.capilux.SharedViewModel
import com.example.capilux.components.CameraPreview
import com.example.capilux.components.PhotoRecommendationDialog
import com.example.capilux.ui.theme.IconTextButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.EncryptedPrefs
import com.example.capilux.utils.FaceFrameAnalyzer
import com.example.capilux.utils.compressImage
import com.example.capilux.utils.takePhoto
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog as MaterialAlertDialog

fun setDialogShown(context: Context, shown: Boolean) {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean("dialog_shown", shown).apply()
}
fun isDialogShown(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean("dialog_shown", false)
}
fun resetDialogFlag(context: Context) {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean("dialog_shown", false).apply()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    username: String,
    profileImageUri: Uri?,
    useAltTheme: Boolean,
    sharedViewModel: SharedViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val gradient = backgroundGradient(useAltTheme)

    // ====== Calidad máxima en captura + análisis liviano ======
    // Preferir la resolución más alta disponible para CAPTURA.
    val captureResSelector = remember {
        ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .build()
    }
    // Mantener análisis en 640x480 aprox. para no restringir la captura.
    val analysisResSelector = remember {
        ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(640, 480),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                )
            ).build()
    }

    // 📷 Controller: IMAGE_CAPTURE + IMAGE_ANALYSIS (ya lo usabas):contentReference[oaicite:3]{index=3}
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            setImageCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            setImageAnalysisBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            // Frontal por defecto
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    // ✅ Estado: rostro dentro del marco
    val faceInsideFrame = remember { mutableStateOf(false) }
    val analysisExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    LaunchedEffect(Unit) {
        cameraController.setImageAnalysisAnalyzer(analysisExecutor, FaceFrameAnalyzer {
            faceInsideFrame.value = it
        })
    }

    // Frontal por defecto en UI
    var isFrontCamera by remember { mutableStateOf(true) } // antes false:contentReference[oaicite:4]{index=4}

    // 📁 Selector de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val compressedUri = compressImage(context, it)
                sharedViewModel.updateImageUri(compressedUri)
                val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("last_captured_image", compressedUri.toString()).apply()
                navController.navigate("confirmPhoto/${Uri.encode(compressedUri.toString())}")
            }
        }
    )

    val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val savedImageUri = remember { sharedPrefs.getString("imageUri", null) }
    val profileUri = savedImageUri?.let { Uri.parse(it) } ?: profileImageUri

    // 💡 Diálogo de recomendaciones inicial
    val showRecommendationsDialog = remember { mutableStateOf(!isDialogShown(context)) }
    if (showRecommendationsDialog.value) {
        PhotoRecommendationDialog(onDismiss = {
            showRecommendationsDialog.value = false
            setDialogShown(context, true)
        })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = Color(0xFF2D0C5A),
                drawerContentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // ===== Header con foto y nombre =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gradient)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = profileUri),
                                    contentDescription = stringResource(R.string.profile_photo),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = stringResource(R.string.profile_photo),
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = username,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== Navegación =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.drawer_section_navigation),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.drawer_home), color = Color.White) },
                            selected = true,
                            onClick = { scope.launch { drawerState.close() } },
                            icon = { Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.drawer_saved_images), color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("savedImages")
                            },
                            icon = { Icon(Icons.Filled.History, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Divider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                        )

                        Text(
                            text = stringResource(R.string.drawer_section_settings),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.drawer_settings), color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("config")
                            },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.drawer_help_support), color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("support")
                            },
                            icon = { Icon(Icons.Filled.Help, contentDescription = null, tint = Color.White) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // ===== Footer =====
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                with(sp.edit()) { remove("username"); remove("imageUri"); apply() }
                                EncryptedPrefs.clearSession(context)
                                navController.navigate("splashDecision") { popUpTo(0) }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x55336699),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.drawer_logout))
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.drawer_version),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = stringResource(R.string.drawer_copyright),
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.app_name),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = stringResource(R.string.open_menu), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconTextButton(
                            text = stringResource(R.string.gallery),
                            icon = { Icon(imageVector = Icons.Filled.Photo, contentDescription = stringResource(R.string.gallery)) },
                            onClick = { galleryLauncher.launch("image/*") }
                        )
                        IconTextButton(
                            text = stringResource(R.string.take_photo),
                            icon = { Icon(imageVector = Icons.Filled.Camera, contentDescription = stringResource(R.string.take_photo)) },
                            onClick = {
                                takePhoto(
                                    cameraController = cameraController,
                                    context = context,
                                    onSuccess = { uri ->
                                        sharedViewModel.updateImageUri(uri)
                                        navController.navigate("confirmPhoto/${Uri.encode(uri.toString())}")
                                    },
                                    onError = { error ->
                                        showErrorDialog = error
                                    }
                                )
                            }
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Diálogo de error
                showErrorDialog?.let { errorMessage ->
                    MaterialAlertDialog(
                        onDismissRequest = { showErrorDialog = null },
                        title = { Text(stringResource(R.string.error)) },
                        text = { Text(errorMessage) },
                        confirmButton = {
                            androidx.compose.material3.Button(onClick = { showErrorDialog = null }) {
                                Text(stringResource(R.string.ok))
                            }
                        }
                    )
                }

                // Saludo
                Text(
                    text = stringResource(R.string.greeting, username),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.main_instruction),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // ===== Vista de cámara =====
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0x552D0C5A),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            cameraController = cameraController
                        )

                        // Óvalo guía simple
                        Canvas(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                        ) {
                            val strokeWidth = 4.dp.toPx()
                            val path = Path().apply {
                                moveTo(size.width / 2f, strokeWidth / 2)
                                cubicTo(
                                    size.width * 0.75f, strokeWidth / 2,
                                    size.width * 0.95f, size.height * 0.25f,
                                    size.width * 0.95f, size.height * 0.55f
                                )
                                cubicTo(
                                    size.width * 0.95f, size.height * 0.85f,
                                    size.width * 0.75f, size.height - strokeWidth / 2,
                                    size.width / 2f, size.height - strokeWidth / 2
                                )
                                cubicTo(
                                    size.width * 0.25f, size.height - strokeWidth / 2,
                                    size.width * 0.05f, size.height * 0.85f,
                                    size.width * 0.05f, size.height * 0.55f
                                )
                                cubicTo(
                                    size.width * 0.05f, size.height * 0.25f,
                                    size.width * 0.25f, strokeWidth / 2,
                                    size.width / 2f, strokeWidth / 2
                                )
                                close()
                            }
                            drawPath(
                                path = path,
                                color = if (faceInsideFrame.value) Color.Green else Color.White,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // Mensaje superior
                        Text(
                            text = if (faceInsideFrame.value)
                                stringResource(R.string.face_ready)
                            else stringResource(R.string.face_not_ready),
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )

                        // Cambiar cámara
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isFrontCamera = !isFrontCamera
                                    cameraController.cameraSelector = if (isFrontCamera) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xAA2D0C5A), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cameraswitch,
                                    contentDescription = stringResource(R.string.switch_camera),
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Indicador modo cámara (opcional)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (isFrontCamera)
                                    stringResource(R.string.front_camera)
                                else
                                    stringResource(R.string.rear_camera),
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xAA2D0C5A), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
