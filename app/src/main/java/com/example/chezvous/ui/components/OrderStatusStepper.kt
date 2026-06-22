package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun OrderStatusStepper(
    status: OrderStatus,
    driverName: String = "",
    modifier: Modifier = Modifier
) {
    val steps = orderStatusSteps(driverName = driverName)
    val currentIndex = steps.indexOfFirst { it.status == status }

    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Text(
                text = stringResource(R.string.order_tracking_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (status == OrderStatus.CANCELLED) {
                Text(
                    text = stringResource(R.string.order_cancelled_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                steps.forEachIndexed { index, step ->
                    StatusStepRow(
                        title = step.title,
                        description = step.description,
                        isDone = index <= currentIndex,
                        isCurrent = index == currentIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusStepRow(
    title: String,
    description: String,
    isDone: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(ChezVousSize.iconLg),
            shape = CircleShape,
            color = if (isDone) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(ChezVousSize.iconSm),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(ChezVousSpacing.md))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderStatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (status == OrderStatus.CANCELLED) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Text(
            text = status.customerLabel(),
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.sm,
                vertical = ChezVousSpacing.xs
            ),
            style = MaterialTheme.typography.labelMedium,
            color = if (status == OrderStatus.CANCELLED) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    }
}

@Composable
fun OrderStatus.customerLabel(): String {
    return when (this) {
        OrderStatus.PENDING -> stringResource(R.string.status_pending)
        OrderStatus.ACCEPTED -> stringResource(R.string.status_accepted)
        OrderStatus.PREPARING -> stringResource(R.string.status_preparing)
        OrderStatus.READY_FOR_PICKUP -> stringResource(R.string.status_ready_for_pickup)
        OrderStatus.PICKED_UP -> stringResource(R.string.status_picked_up)
        OrderStatus.ON_THE_WAY -> stringResource(R.string.status_on_the_way)
        OrderStatus.DELIVERED -> stringResource(R.string.status_delivered)
        OrderStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    }
}

private data class OrderStatusStep(
    val status: OrderStatus,
    val title: String,
    val description: String
)

@Composable
private fun orderStatusSteps(driverName: String): List<OrderStatusStep> {
    return listOf(
        OrderStatusStep(
            status = OrderStatus.PENDING,
            title = stringResource(R.string.order_step_pending_title),
            description = stringResource(R.string.order_step_pending_description)
        ),
        OrderStatusStep(
            status = OrderStatus.ACCEPTED,
            title = stringResource(R.string.order_step_accepted_title),
            description = stringResource(R.string.order_step_accepted_description)
        ),
        OrderStatusStep(
            status = OrderStatus.PREPARING,
            title = stringResource(R.string.order_step_preparing_title),
            description = stringResource(R.string.order_step_preparing_description)
        ),
        OrderStatusStep(
            status = OrderStatus.READY_FOR_PICKUP,
            title = stringResource(R.string.order_step_ready_title),
            description = stringResource(R.string.order_step_ready_description)
        ),
        OrderStatusStep(
            status = OrderStatus.PICKED_UP,
            title = stringResource(R.string.order_step_picked_up_title),
            description = if (driverName.isNotBlank()) {
                stringResource(R.string.order_step_picked_up_description_with_driver, driverName)
            } else {
                stringResource(R.string.order_step_picked_up_description)
            }
        ),
        OrderStatusStep(
            status = OrderStatus.ON_THE_WAY,
            title = stringResource(R.string.order_step_on_the_way_title),
            description = if (driverName.isNotBlank()) {
                stringResource(R.string.order_step_on_the_way_description_with_driver, driverName)
            } else {
                stringResource(R.string.order_step_on_the_way_description)
            }
        ),
        OrderStatusStep(
            status = OrderStatus.DELIVERED,
            title = stringResource(R.string.order_step_delivered_title),
            description = if (driverName.isNotBlank()) {
                stringResource(R.string.order_step_delivered_description_with_driver, driverName)
            } else {
                stringResource(R.string.order_step_delivered_description)
            }
        )
    )
}
