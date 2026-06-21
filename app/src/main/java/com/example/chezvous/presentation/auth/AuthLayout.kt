package com.example.chezvous.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chezvous.R
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousEmailField
import com.example.chezvous.ui.components.ChezVousPasswordField
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.GoogleSignInButton
import com.example.chezvous.ui.components.OrDivider
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun AuthLayout(
    title: String,
    subtitle: String,
    fullName: String? = null,
    email: String,
    password: String,
    showPassword: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    mainButtonText: String,
    loadingText: String,
    switchText: String,
    onFullNameChange: ((String) -> Unit)? = null,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onMainClick: () -> Unit,
    onSwitchClick: () -> Unit,
    onGoogleClick: (() -> Unit)? = null
) {
    val requiresFullName = fullName != null
    val canSubmit = email.isNotBlank() &&
            password.length >= 6 &&
            (!requiresFullName || !fullName.isNullOrBlank()) &&
            !isLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeaderBand(subtitle = subtitle)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ChezVousSize.maxFormWidth)
                    .padding(horizontal = ChezVousSpacing.sheetHorizontal)
                    .padding(vertical = ChezVousSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.lg))

                if (fullName != null && onFullNameChange != null) {
                    ChezVousTextField(
                        value = fullName,
                        onValueChange = onFullNameChange,
                        label = "Nom complet",
                        leadingIcon = Icons.Outlined.Person
                    )

                    Spacer(modifier = Modifier.height(ChezVousSpacing.sm))
                }

                ChezVousEmailField(
                    value = email,
                    onValueChange = onEmailChange
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                ChezVousPasswordField(
                    value = password,
                    onValueChange = onPasswordChange,
                    showPassword = showPassword,
                    onTogglePassword = onTogglePassword
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(ChezVousSpacing.xl))

                ChezVousButton(
                    text = mainButtonText,
                    loadingText = loadingText,
                    isLoading = isLoading,
                    enabled = canSubmit,
                    onClick = onMainClick
                )

                if (onGoogleClick != null) {
                    Spacer(modifier = Modifier.height(ChezVousSpacing.md))

                    OrDivider()

                    Spacer(modifier = Modifier.height(ChezVousSpacing.md))

                    GoogleSignInButton(
                        enabled = !isLoading,
                        onClick = onGoogleClick
                    )
                }

                Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                TextButton(
                    onClick = onSwitchClick,
                    enabled = !isLoading
                ) {
                    Text(text = switchText)
                }
            }
        }
    }
}

@Composable
private fun AuthHeaderBand(subtitle: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ChezVousSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo ChezVous",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
