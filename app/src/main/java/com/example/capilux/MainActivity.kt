package com.example.capilux

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.capilux.navigation.AppNavigation
import com.example.capilux.screen.resetDialogFlag
import com.example.capilux.ui.theme.CapiluxTheme
import com.example.capilux.utils.getInitialDarkModePreference
import com.example.capilux.utils.getInitialThemePreference
import com.example.capilux.utils.getInitialThemePreference
import com.example.capilux.utils.isCameraPermissionGranted
import com.example.capilux.utils.requestCameraPermission

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val darkModeState = remember {
                mutableStateOf(getInitialDarkModePreference(this))
            }
            val altThemeState = remember {
                mutableStateOf(getInitialThemePreference(this))
            }

            // Verificar si hay un usuario guardado
            val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val username = sharedPrefs.getString("username", null)
            val usernameState = remember { mutableStateOf(username ?: "") }
            if (!isCameraPermissionGranted(this)) {
                // Solicitar permisos si no están concedidos
                requestCameraPermission(this)
            }
            CapiluxTheme(darkTheme = darkModeState.value) {
                val startDestination = if (username != null) "login" else "explanation"
                AppNavigation(darkModeState, altThemeState, usernameState, startDestination = startDestination)
                }
        }
    }
    override fun onStop() {
        super.onStop()
        // Restablecer la bandera cuando la app se cierra
        resetDialogFlag(this)  // Aquí restablecemos la bandera en SharedPreferences
    }
}
