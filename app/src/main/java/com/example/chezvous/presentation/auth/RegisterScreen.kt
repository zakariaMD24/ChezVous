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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val onGoogleClick = rememberGoogleSignInHandler(
        onIdToken = viewModel::loginWithGoogle,
        onError = viewModel::showError
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onRegisterSuccess()
    }

    AuthLayout(
        title = "Creer un compte",
        subtitle = "Commandez vos repas facilement",
        fullName = fullName,
        email = email,
        password = password,
        showPassword = showPassword,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        mainButtonText = "S'inscrire",
        loadingText = "Creation...",
        switchText = "J'ai deja un compte",
        onFullNameChange = { fullName = it },
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onTogglePassword = { showPassword = !showPassword },
        onMainClick = { viewModel.register(fullName.trim(), email.trim(), password) },
        onSwitchClick = onGoToLogin,
        onGoogleClick = onGoogleClick
    )
}
