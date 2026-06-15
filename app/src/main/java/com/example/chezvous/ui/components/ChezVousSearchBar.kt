package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.chezvous.R

@Composable
fun ChezVousSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    modifier: Modifier = Modifier
) {
    val resolvedPlaceholder = placeholder ?: stringResource(R.string.search_restaurant_default)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(resolvedPlaceholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        },
        singleLine = true,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    )
}
