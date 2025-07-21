package com.example.capilux.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.capilux.components.CameraPreview
import com.example.capilux.components.PhotoRecommendationDialog
import com.example.capilux.ui.theme.IconTextButton
import com.example.capilux.ui.theme.backgroundGradient
import com.example.capilux.utils.FaceFrameAnalyzer
import com.example.capilux.utils.compressImage
import com.example.capilux.utils.takePhoto
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog as MaterialAlertDialog

// Guarda si el diálogo ya fue mostrado
fun setDialogShown(context: Context, shown: Boolean) {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putBoolean("dialog_shown", shown).apply()
}

// Verifica si el diálogo ya fue mostrado
fun isDialogShown(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean("dialog_shown", false)
}

// Restablecer la bandera al cerrar la app
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
    useAltTheme: Boolean
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val gradient = backgroundGradient(useAltTheme)

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            // Configurar casos de uso para captura y análisis en tiempo real
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
        }
    }
    val faceInsideFrame = remember { mutableStateOf(false) }
    val analysisExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    LaunchedEffect(Unit) {
        cameraController.setImageAnalysisAnalyzer(analysisExecutor, FaceFrameAnalyzer {
            faceInsideFrame.value = it
        })
    }
    var isFrontCamera by remember { mutableStateOf(false) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val compressedUri = compressImage(context, it)
                val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("last_captured_image", compressedUri.toString())
                    .apply()
                navController.navigate("confirmPhoto/${Uri.encode(compressedUri.toString())}")
            }
        }
    )
    val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val savedImageUri = remember { sharedPrefs.getString("imageUri", null) }
    val profileUri = savedImageUri?.let { Uri.parse(it) } ?: profileImageUri
    val showRecommendationsDialog = remember { mutableStateOf(!isDialogShown(context)) }
    if (showRecommendationsDialog.value) {
        PhotoRecommendationDialog(onDismiss = {
            showRecommendationsDialog.value = false
            setDialogShown(context, true) // Marca que el diálogo ha sido mostrado
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
                        .verticalScroll(rememberScrollState()) // Esto agrega scroll
                        .padding(16.dp)
                ) {
                    // Encabezado con foto de perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gradient)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Foto de perfil
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
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Foto de perfil",
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre de usuario
                        Text(
                            text = username,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección principal
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Navegación principal
                        Text(
                            text = "NAVEGACIÓN",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text("Inicio", color = Color.White) },
                            selected = true,
                            onClick = { scope.launch { drawerState.close() } },
                            icon = {
                                Icon(
                                    Icons.Filled.Home,
                                    contentDescription = "Inicio",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text("Estilos favoritos", color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("favorites") // Navegar a la pantalla de favoritos
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "Estilos favoritos",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Divider
                        Divider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                        )

                        // Configuración y cuenta
                        Text(
                            text = "CONFIGURACIÓN",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text("Configuración", color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("config")
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Configuración",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        NavigationDrawerItem(
                            label = { Text("Ayuda y soporte", color = Color.White) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("support")
                            },
                            icon = {
                                Icon(
                                    Icons.Filled.Help,
                                    contentDescription = "Ayuda",
                                    tint = Color.White
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Pie de página
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Botón de cerrar sesión
                        Button(
                            onClick = {
                                // Lógica para cerrar sesión
                                val sharedPrefs =
                                    context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                with(sharedPrefs.edit()) {
                                    remove("username")
                                    remove("imageUri")
                                    apply()
                                }
                                EncryptedPrefs.clearSession(context)

                                // Volver al inicio y limpiar el backstack
                                navController.navigate("splashDecision") {
                                    popUpTo(0)
                                }

                                // Cerrar el menú lateral
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
                            Icon(
                                imageVector = Icons.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar sesión")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Versión de la app
                        Text(
                            text = "Capilux v0.26.0",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )

                        // Información de derechos
                        Text(
                            text = "© 2025 Capilux. Todos los derechos reservados.",
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
                            "Capilux",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Abrir menú",
                                tint = Color.White
                            )
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
                            text = "Galería",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Photo,
                                    contentDescription = "Galería"
                                )
                            },
                            onClick = { galleryLauncher.launch("image/*") }
                        )

                        IconTextButton(
                            text = "Tomar foto",
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Camera,
                                    contentDescription = "Tomar foto"
                                )
                            },
                            onClick = {
                                takePhoto(
                                    cameraController = cameraController,
                                    context = context,
                                    onSuccess = { uri ->
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
                        title = { Text("Error") },
                        text = { Text(errorMessage) },
                        confirmButton = {
                            androidx.compose.material3.Button(onClick = { showErrorDialog = null }) {
                                Text("OK")
                            }
                        }
                    )
                }

                // Mensaje de bienvenida
                Text(
                    text = "Hola, $username",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Captura tu rostro o selecciona una foto para analizar tu tipo de rostro",
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Contenedor de la cámara
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

                        // Marco guía con forma de rostro
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
                                    size.width * 0.75f,
                                    strokeWidth / 2,
                                    size.width * 0.95f,
                                    size.height * 0.25f,
                                    size.width * 0.95f,
                                    size.height * 0.55f
                                )
                                cubicTo(
                                    size.width * 0.95f,
                                    size.height * 0.85f,
                                    size.width * 0.75f,
                                    size.height - strokeWidth / 2,
                                    size.width / 2f,
                                    size.height - strokeWidth / 2
                                )
                                cubicTo(
                                    size.width * 0.25f,
                                    size.height - strokeWidth / 2,
                                    size.width * 0.05f,
                                    size.height * 0.85f,
                                    size.width * 0.05f,
                                    size.height * 0.55f
                                )
                                cubicTo(
                                    size.width * 0.05f,
                                    size.height * 0.25f,
                                    size.width * 0.25f,
                                    strokeWidth / 2,
                                    size.width / 2f,
                                    strokeWidth / 2
                                )
                                close()
                            }
                            drawPath(
                                path = path,
                                color = if (faceInsideFrame.value) Color.Green else Color.White,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        Text(
                            text = if (faceInsideFrame.value) "Puedes tomar la foto" else "Coloca tu cara en el marco",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )

                        // Botón para cambiar cámara
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
                                    .background(
                                        Color(0xAA2D0C5A),
                                        CircleShape
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cameraswitch,
                                    contentDescription = "Cambiar cámara",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Indicador de modo cámara
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (isFrontCamera) "Cámara frontal" else "Cámara trasera",
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        Color(0xAA2D0C5A),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                        }
                    }
                }
            }
