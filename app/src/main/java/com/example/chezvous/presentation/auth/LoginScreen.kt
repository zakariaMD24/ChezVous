package com.example.chezvous.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R

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
        switchText = "Créer un compte",
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onTogglePassword = { showPassword = !showPassword },
        onMainClick = { viewModel.login(email.trim(), password) },
        onSwitchClick = onGoToRegister
    )
}