package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.chezvous.ui.theme.ChezVousSpacing

class FilterOptionChipItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun FilterOptionChipRow(
    options: List<FilterOptionChipItem>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
    ) {
        items(options) { option ->
            CategoryChip(
                text = option.label,
                selected = option.selected,
                onClick = option.onClick
            )
        }
    }
}
