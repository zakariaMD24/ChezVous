package com.example.chezvous

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chezvous.navigation.ChezVousNavHost
import com.example.chezvous.ui.theme.ChezVousTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChezVousTheme {
                ChezVousNavHost()
            }
        }
    }
}
