package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ChezVousTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(icon, contentDescription = null)
            }
        },
        singleLine = singleLine,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        minLines = if (singleLine) 1 else 3,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        )
    )
}

@Composable
fun ChezVousEmailField(
    value: String,
    onValueChange: (String) -> Unit
) {
    ChezVousTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Adresse email",
        leadingIcon = Icons.Outlined.Email,
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next
    )
}
