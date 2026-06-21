package com.example.chezvous.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.R
import com.example.chezvous.presentation.auth.rememberCredentialLogoutHandler
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.ChezVousTopBar
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing
import androidx.compose.ui.res.stringResource

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    onLoggedOut: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val onLogout = rememberCredentialLogoutHandler(
        onLogout = viewModel::logout
    )

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLoggedOut()
    }

    val resolvedOnBack = if (showBackButton) onBack else null

    ProfileContent(
        uiState = uiState,
        onBack = resolvedOnBack,
        onFullNameChange = viewModel::onFullNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onAddressChange = viewModel::onAddressChange,
        onSave = viewModel::saveProfile,
        onLogout = onLogout
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onBack: (() -> Unit)?,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            ChezVousTopBar(
                title = stringResource(R.string.profile_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, contentDescription = stringResource(R.string.profile_logout_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(ChezVousSpacing.xl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .chezVousScreenPadding()
                    .padding(vertical = ChezVousSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
            ) {
                Text(
                    text = stringResource(R.string.profile_user_info_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = stringResource(R.string.profile_user_info_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                ChezVousTextField(
                    value = uiState.fullName,
                    onValueChange = onFullNameChange,
                    label = stringResource(R.string.profile_full_name),
                    leadingIcon = Icons.Outlined.Person
                )

                ChezVousTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = stringResource(R.string.profile_email),
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    enabled = false
                )

                ChezVousTextField(
                    value = uiState.phone,
                    onValueChange = onPhoneChange,
                    label = stringResource(R.string.profile_phone),
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )

                ChezVousTextField(
                    value = uiState.address,
                    onValueChange = onAddressChange,
                    label = stringResource(R.string.profile_address),
                    leadingIcon = Icons.Outlined.Home,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    singleLine = false
                )

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                uiState.successMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ChezVousButton(
                    text = stringResource(R.string.save),
                    loadingText = stringResource(R.string.profile_saving),
                    isLoading = uiState.isSaving,
                    enabled = uiState.fullName.isNotBlank(),
                    onClick = onSave
                )

                OutlinedButton(onClick = onLogout) {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Text(stringResource(R.string.profile_logout))
                }
            }
        }
    }
}
