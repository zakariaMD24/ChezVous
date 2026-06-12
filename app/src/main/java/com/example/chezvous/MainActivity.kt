package com.example.chezvous

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.chezvous.presentation.auth.AuthScreenState
import com.example.chezvous.presentation.auth.LoginScreen
import com.example.chezvous.presentation.auth.RegisterScreen
import com.example.chezvous.presentation.home.HomeScreen
import com.example.chezvous.ui.theme.ChezVousTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChezVousTheme {
                var currentScreen by remember {
                    mutableStateOf(AuthScreenState.LOGIN)
                }

                when (currentScreen) {
                    AuthScreenState.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                currentScreen = AuthScreenState.HOME
                            },
                            onGoToRegister = {
                                currentScreen = AuthScreenState.REGISTER
                            }
                        )
                    }

                    AuthScreenState.REGISTER -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                currentScreen = AuthScreenState.HOME
                            },
                            onGoToLogin = {
                                currentScreen = AuthScreenState.LOGIN
                            }
                        )
                    }

                    AuthScreenState.HOME -> {
                        HomeScreen()
                    }

                    else -> {}
                }
            }
        }
    }
}