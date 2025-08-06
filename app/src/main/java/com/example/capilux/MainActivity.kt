package com.example.capilux

import android.os.Bundle
import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.capilux.navigation.AppNavigation
import com.example.capilux.screen.resetDialogFlag
import com.example.capilux.ui.theme.CapiluxTheme
import com.example.capilux.utils.*
import com.example.capilux.SharedViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "es") ?: "es"
        setAppLocale(this, language)

        // Usamos FragmentActivity para mostrar Compose manualmente
        setContent {
            val darkModeState = remember {
                mutableStateOf(getInitialDarkModePreference(this))
            }
            val altThemeState = remember {
                mutableStateOf(getInitialThemePreference(this))
            }

            val username = EncryptedPrefs.getUsername(this)
            val usernameState = remember { mutableStateOf(username ?: "") }
            val sharedViewModel: SharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            if (!isCameraPermissionGranted(this)) {
                requestCameraPermission(this)
            }

            CapiluxTheme(darkTheme = darkModeState.value) {
                AppNavigation(darkModeState, altThemeState, usernameState, sharedViewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        resetDialogFlag(this)
    }
}
