package com.example.chezvous.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.ui.components.ChezVousTextField
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun AdminAddRestaurantScreen(
    onSaved: () -> Unit,
    viewModel: AdminAddRestaurantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .chezVousScreenPadding()
            .padding(vertical = ChezVousSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        FormSectionHeader(title = "Informations générales")

        // Name — required
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Nom du restaurant *") },
            leadingIcon = {
                Icon(Icons.Outlined.Restaurant, contentDescription = null)
            },
            isError = uiState.nameError,
            supportingText = if (uiState.nameError) {
                { Text("Le nom est obligatoire") }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Cuisine type — required
        OutlinedTextField(
            value = uiState.cuisineType,
            onValueChange = viewModel::onCuisineTypeChange,
            label = { Text("Type de cuisine *") },
            leadingIcon = {
                Icon(Icons.Outlined.Restaurant, contentDescription = null)
            },
            isError = uiState.cuisineError,
            supportingText = if (uiState.cuisineError) {
                { Text("Le type de cuisine est obligatoire") }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        HorizontalDivider()
        FormSectionHeader(title = "Livraison")

        // Delivery time — required, free text (e.g. "30-45 min")
        OutlinedTextField(
            value = uiState.deliveryTime,
            onValueChange = viewModel::onDeliveryTimeChange,
            label = { Text("Délai de livraison * (ex: 30-45 min)") },
            leadingIcon = {
                Icon(Icons.Outlined.AccessTime, contentDescription = null)
            },
            isError = uiState.deliveryError,
            supportingText = if (uiState.deliveryError) {
                { Text("Le délai de livraison est obligatoire") }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        // Minimum order — optional numeric, validated if non-empty
        OutlinedTextField(
            value = uiState.minimumOrder,
            onValueChange = viewModel::onMinimumOrderChange,
            label = { Text("Commande minimum (DA)") },
            leadingIcon = {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null)
            },
            isError = uiState.minimumOrderError,
            supportingText = if (uiState.minimumOrderError) {
                { Text("Valeur numérique invalide") }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        HorizontalDivider()
        FormSectionHeader(title = "Médias")

        // Image URL — optional
        ChezVousTextField(
            value = uiState.imageUrl,
            onValueChange = viewModel::onImageUrlChange,
            label = "URL de l'image (optionnel)",
            leadingIcon = Icons.Outlined.Image,
            imeAction = ImeAction.Done
        )

        HorizontalDivider()
        FormSectionHeader(title = "Disponibilité")

        // isOpen toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ChezVousSpacing.md, vertical = ChezVousSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Restaurant ouvert",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (uiState.isOpen) "Visible et accessible aux clients" else "Masqué aux clients",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isOpen,
                    onCheckedChange = viewModel::onIsOpenChange
                )
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(ChezVousSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

        // Save button
        Button(
            onClick = viewModel::save,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving,
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Enregistrer le restaurant",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(ChezVousSpacing.lg))
    }
}

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}
