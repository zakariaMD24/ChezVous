package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.ui.theme.ChezVousSpacing
import androidx.compose.ui.unit.dp

@Composable
fun PartnerMenuItemCard(
    foodItem: FoodItem,
    onAvailabilityChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val removableCount = foodItem.removableIngredientOptions
        .ifEmpty {
            foodItem.removableIngredients.map { ingredient ->
                com.example.chezvous.data.model.CustomizationOption(name = ingredient)
            }
        }
        .size
    val spiceCount = if (foodItem.hasSpiceLevelSelection()) 4 else 0

    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.size(84.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model = foodItem.displayFoodImageUrl(),
                        contentDescription = foodItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(ChezVousSpacing.sm))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = foodItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${foodItem.category} - ${foodItem.price.asDhPrice()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(
                            R.string.customization_short_summary,
                            foodItem.extraOptions.size,
                            removableCount,
                            spiceCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = foodItem.isAvailable,
                    onCheckedChange = onAvailabilityChange,
                    enabled = !isSaving
                )
            }

            Spacer(modifier = Modifier.size(ChezVousSpacing.sm))

            OutlinedButton(
                onClick = onEditClick,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(stringResource(R.string.edit))
            }
        }
    }
}

private fun FoodItem.hasSpiceLevelSelection(): Boolean {
    return isSpiceLevelEnabled || spiceLevelOptions.isNotEmpty() || spiceLevels.isNotEmpty()
}
