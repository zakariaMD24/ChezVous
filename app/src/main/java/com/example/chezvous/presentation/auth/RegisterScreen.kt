package com.example.chezvous.presentation.auth

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    AuthLayout(
        title = "Créer un compte",
        subtitle = "Commandez vos repas facilement",
        email = email,
        password = password,
        showPassword = showPassword,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        mainButtonText = "S’inscrire",
        loadingText = "Création...",
        switchText = "J’ai déjà un compte",
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onTogglePassword = { showPassword = !showPassword },
        onMainClick = { viewModel.register(email.trim(), password) },
        onSwitchClick = onGoToLogin
    )
}