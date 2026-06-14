package com.example.chezvous.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.example.chezvous.R
import com.example.chezvous.ui.components.ChezVousButton
import com.example.chezvous.ui.components.ChezVousEmailField
import com.example.chezvous.ui.components.ChezVousPasswordField
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.GoogleSignInButton

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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo ChezVous",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (fullName != null && onFullNameChange != null) {
                ChezVousTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = "Nom complet",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            ChezVousEmailField(
                value = email,
                onValueChange = onEmailChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChezVousPasswordField(
                value = password,
                onValueChange = onPasswordChange,
                showPassword = showPassword,
                onTogglePassword = onTogglePassword
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            ChezVousButton(
                text = mainButtonText,
                loadingText = loadingText,
                isLoading = isLoading,
                enabled = canSubmit,
                onClick = onMainClick
            )

            if (onGoogleClick != null) {
                Spacer(modifier = Modifier.height(10.dp))

                GoogleSignInButton(
                    enabled = !isLoading,
                    onClick = onGoogleClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onSwitchClick,
                enabled = !isLoading
            ) {
                Text(text = switchText)
            }
        }
    }
}
