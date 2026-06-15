package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.chezvous.R
import com.example.chezvous.ui.theme.ChezVousSpacing

class ActiveFilterChipItem(
    val label: String,
    val onRemove: () -> Unit
)

@Composable
fun ActiveFilterChipRow(
    filters: List<ActiveFilterChipItem>,
    onClearAll: () -> Unit
) {
    if (filters.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
        ) {
            items(filters) { filter ->
                InputChip(
                    selected = true,
                    onClick = filter.onRemove,
                    label = { Text(filter.label) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.remove_filter)
                        )
                    }
                )
            }
        }

        TextButton(onClick = onClearAll) {
            Text(stringResource(R.string.clear_all))
        }
    }
}
