package com.example.capilux

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.capilux.navigation.AppNavigation
import com.example.capilux.screen.resetDialogFlag
import com.example.capilux.ui.theme.CapiluxTheme
import com.example.capilux.utils.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Usamos FragmentActivity para mostrar Compose manualmente
        setContent {
            val darkModeState = remember {
                mutableStateOf(getInitialDarkModePreference(this))
            }
            val altThemeState = remember {
                mutableStateOf(getInitialThemePreference(this))
            }

            val username = EncryptedPrefs.getPrefs(this).getString("username", null)
            val usernameState = remember { mutableStateOf(username ?: "") }

            if (!isCameraPermissionGranted(this)) {
                requestCameraPermission(this)
            }

            CapiluxTheme(darkTheme = darkModeState.value) {
                AppNavigation(darkModeState, altThemeState, usernameState)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        resetDialogFlag(this)
    }
}
