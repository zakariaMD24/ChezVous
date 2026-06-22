package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.chezvous.R
import com.example.chezvous.data.model.Driver
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun DriverCard(
    driver: Driver?,
    estimatedDeliveryTime: String,
    modifier: Modifier = Modifier
) {
    ChezVousCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(ChezVousSize.imageSm),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeliveryDining,
                    contentDescription = null,
                    modifier = Modifier.padding(ChezVousSpacing.sm),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.size(ChezVousSpacing.md))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = driver?.fullName ?: stringResource(R.string.driver_waiting_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (driver == null) {
                        stringResource(R.string.driver_waiting_message)
                    } else {
                        "${driver.vehicleType} - ${driver.phone}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (estimatedDeliveryTime.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.driver_estimation, estimatedDeliveryTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (driver != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(ChezVousSize.iconSm),
                        tint = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.size(ChezVousSpacing.xxs))

                    Text(
                        text = if (driver.ratingCount > 0) {
                            stringResource(
                                R.string.driver_rating_with_count,
                                driver.rating,
                                driver.ratingCount
                            )
                        } else {
                            stringResource(R.string.driver_new_rating)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
