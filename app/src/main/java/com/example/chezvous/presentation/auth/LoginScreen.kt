package com.example.chezvous.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val onGoogleClick = rememberGoogleSignInHandler(
        onIdToken = viewModel::loginWithGoogle,
        onError = viewModel::showError
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    AuthLayout(
        title = "Connexion",
        subtitle = "Bienvenue sur ChezVous",
        email = email,
        password = password,
        showPassword = showPassword,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        mainButtonText = "Se connecter",
        loadingText = "Connexion...",
        switchText = "Creer un compte",
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onTogglePassword = { showPassword = !showPassword },
        onMainClick = { viewModel.login(email.trim(), password) },
        onSwitchClick = onGoToRegister,
        onGoogleClick = onGoogleClick
    )
}
