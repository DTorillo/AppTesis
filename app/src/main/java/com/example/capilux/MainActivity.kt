package com.example.capilux

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.camera.view.LifecycleCameraController
import com.example.capilux.navigation.AppNavigation
import com.example.capilux.screen.resetDialogFlag
import com.example.capilux.ui.theme.CapiluxTheme
import com.example.capilux.utils.EncryptedPrefs
import com.example.capilux.utils.getInitialDarkModePreference
import com.example.capilux.utils.getInitialThemePreference
import com.example.capilux.utils.isCameraPermissionGranted
import com.example.capilux.utils.requestCameraPermission
import com.example.capilux.utils.setAppLocale
import java.util.concurrent.Executor

// 1) Exponemos controller y executor globalmente para poder usarlos desde cualquier pantalla.
object ImageCaptureDeps {
    lateinit var controller: LifecycleCameraController
    lateinit var executor: Executor
}

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "es") ?: "es"
        setAppLocale(this, language)

        // 2) Crear y configurar el controller de CameraX.
        val controller = LifecycleCameraController(this).apply {
            // configureController(...) viene de tu ImageCaptureHelper.kt
            configureController(this)
            // Vinculamos el controller al ciclo de vida de la Activity
            bindToLifecycle(this@MainActivity)
        }

        // 3) Executor para callbacks de captura (main thread)
        val mainExecutor = ContextCompat.getMainExecutor(this)

        // 4) Hacemos disponibles las dependencias para el resto de la app
        ImageCaptureDeps.controller = controller
        ImageCaptureDeps.executor = mainExecutor

        // 5) Permisos de cámara (si falta, lo pedimos)
        if (!isCameraPermissionGranted(this)) {
            requestCameraPermission(this)
        }

        // 6) UI Compose intacta
        setContent {
            val darkModeState = remember { mutableStateOf(getInitialDarkModePreference(this)) }
            val altThemeState = remember { mutableStateOf(getInitialThemePreference(this)) }

            val username = EncryptedPrefs.getUsername(this)
            val usernameState = remember { mutableStateOf(username ?: "") }
            val sharedViewModel: SharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            CapiluxTheme(darkTheme = darkModeState.value) {
                // Si lo prefieres, también puedes pasar controller/executor a tu Nav:
                // AppNavigation(darkModeState, altThemeState, usernameState, sharedViewModel, controller, mainExecutor)
                AppNavigation(darkModeState, altThemeState, usernameState, sharedViewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        resetDialogFlag(this)
    }

    // (Opcional) Si manejas el callback de permisos aquí, podrías re-vincular o reintentar.
    // override fun onRequestPermissionsResult(...) { ... }
}
