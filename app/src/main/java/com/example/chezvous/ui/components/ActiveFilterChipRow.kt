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
import androidx.compose.ui.unit.dp

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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                InputChip(
                    selected = true,
                    onClick = filter.onRemove,
                    label = { Text(filter.label) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Retirer"
                        )
                    }
                )
            }
        }

        TextButton(onClick = onClearAll) {
            Text("Tout effacer")
        }
    }
}
