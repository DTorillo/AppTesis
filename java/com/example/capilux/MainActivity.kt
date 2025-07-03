package com.example.capilux

import android.content.Context
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.capilux.navigation.AppNavigation
import com.example.capilux.screen.resetDialogFlag
import com.example.capilux.ui.theme.CapiluxTheme
import com.example.capilux.utils.getInitialDarkModePreference
import com.example.capilux.utils.isCameraPermissionGranted
import com.example.capilux.utils.requestCameraPermission

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        authenticate {
            setContent {
                val darkModeState = remember {
                    mutableStateOf(getInitialDarkModePreference(this))
                }

                // Verificar si hay un usuario guardado
                val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val username = sharedPrefs.getString("username", null)
                if (!isCameraPermissionGranted(this)) {
                    // Solicitar permisos si no están concedidos
                    requestCameraPermission(this)
                }
                CapiluxTheme(darkTheme = darkModeState.value) {
                    if (username != null) {
                        // Si hay usuario, ir directamente a MainScreen
                        AppNavigation(darkModeState, startDestination = "main/$username")
                    } else {
                        // Si no, comenzar en ExplanationScreen
                        AppNavigation(darkModeState)
                    }
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        // Restablecer la bandera cuando la app se cierra
        resetDialogFlag(this)  // Aquí restablecemos la bandera en SharedPreferences
    }

    private fun authenticate(onAuthenticated: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(this)
            val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }
            })

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación requerida")
                .setSubtitle("Confirma tu huella digital")
                .setNegativeButtonText("Cancelar")
                .build()

            prompt.authenticate(info)
        } else {
            onAuthenticated()
        }
    }
}
