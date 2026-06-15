package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null
) {
    ChezVousCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            FoodItemImage(foodItem = foodItem)

            Spacer(modifier = Modifier.width(ChezVousSpacing.md))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row {
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = foodItem.price.asDhPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                Text(
                    text = foodItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

                Row {
                    Text(
                        text = foodItem.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = if (foodItem.isAvailable) {
                            stringResource(R.string.available)
                        } else {
                            stringResource(R.string.unavailable)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (foodItem.isAvailable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                if (onAddClick != null) {
                    Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                    FilledTonalButton(
                        onClick = onAddClick,
                        enabled = foodItem.isAvailable,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(ChezVousSize.iconMd)
                        )

                        Spacer(modifier = Modifier.width(ChezVousSpacing.xs))

                        Text(
                            text = if (foodItem.isAvailable) {
                                stringResource(R.string.customize)
                            } else {
                                stringResource(R.string.unavailable)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemImage(foodItem: FoodItem) {
    Surface(
        modifier = Modifier.size(ChezVousSize.imageLg),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        AsyncImage(
            model = foodItem.displayFoodImageUrl(),
            contentDescription = foodItem.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
