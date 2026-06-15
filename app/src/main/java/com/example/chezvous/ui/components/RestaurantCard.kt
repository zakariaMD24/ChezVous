package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chezvous.R
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    ChezVousCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.sm)
        ) {
            RestaurantImage(restaurant = restaurant)

            Spacer(modifier = Modifier.width(ChezVousSpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                Text(
                    text = restaurant.cuisineType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.xs))

                RestaurantInfoLine(restaurant = restaurant)

                Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                Text(
                    text = stringResource(
                        R.string.minimum_order_format,
                        restaurant.minimumOrder.asDhPrice()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RestaurantStatusChip(isOpen = restaurant.isOpen)
                    RestaurantActionChip()
                }
            }
        }
    }
}

@Composable
private fun RestaurantImage(restaurant: Restaurant) {
    Surface(
        modifier = Modifier
            .width(112.dp)
            .height(112.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        if (restaurant.imageUrl.isNotBlank()) {
            AsyncImage(
                model = restaurant.imageUrl,
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                modifier = Modifier.padding(ChezVousSpacing.lg),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun RestaurantInfoLine(restaurant: Restaurant) {
    Row {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            modifier = Modifier.size(ChezVousSize.iconSm),
            tint = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.width(ChezVousSpacing.xxs))

        Text(
            text = restaurant.rating.toString(),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.width(ChezVousSpacing.sm))

        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(ChezVousSize.iconSm),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(ChezVousSpacing.xxs))

        Text(
            text = restaurant.deliveryTime,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun RestaurantStatusChip(isOpen: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isOpen) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(
            text = if (isOpen) stringResource(R.string.restaurant_open) else stringResource(R.string.restaurant_closed),
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.xs,
                vertical = ChezVousSpacing.xxs
            ),
            style = MaterialTheme.typography.bodySmall,
            color = if (isOpen) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            }
        )
    }
}

@Composable
private fun RestaurantActionChip() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.xs,
                vertical = ChezVousSpacing.xxs
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.DeliveryDining,
                contentDescription = null,
                modifier = Modifier.size(ChezVousSize.iconSm),
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.width(ChezVousSpacing.xxs))

            Text(
                text = stringResource(R.string.see_menu),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
