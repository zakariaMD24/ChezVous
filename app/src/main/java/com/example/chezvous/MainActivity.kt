package com.example.chezvous

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.chezvous.navigation.ChezVousNavHost
import com.example.chezvous.ui.theme.AppThemeController
import com.example.chezvous.ui.theme.ChezVousTheme
import com.example.chezvous.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeController.initialize(applicationContext)

        setContent {
            val themeMode by AppThemeController.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            ChezVousTheme(darkTheme = darkTheme) {
                ChezVousNavHost()
            }
        }
    }
}
