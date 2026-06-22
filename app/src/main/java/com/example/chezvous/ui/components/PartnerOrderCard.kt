package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.chezvous.R
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun PartnerOrderCard(
    order: Order,
    nextStatus: OrderStatus?,
    onUpdateStatus: (OrderStatus) -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val missingAddressText = stringResource(R.string.delivery_address_missing)

    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.partner_order_title, order.id.takeLast(6)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = stringResource(
                            R.string.partner_order_summary,
                            order.items.sumOf { it.quantity },
                            order.totalPrice.asDhPrice()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            Text(
                text = order.deliveryAddress.ifBlank { missingAddressText },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            order.items
                .filter {
                    it.selectedExtras.isNotEmpty() ||
                            it.removedIngredients.isNotEmpty() ||
                            it.spiceLevel.isNotBlank() ||
                            it.specialInstruction.isNotBlank()
                }
                .forEach { item ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
                    ) {
                        Text(
                            text = item.foodItem.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (item.selectedExtras.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.extras_summary,
                                    item.selectedExtras.joinToString { it.name }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (item.removedIngredients.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.removed_ingredients_summary,
                                    item.removedIngredients.joinToString()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (item.spiceLevel.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.spice_summary, item.spiceLevel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (item.specialInstruction.isNotBlank()) {
                            Text(
                                text = stringResource(
                                    R.string.item_instruction_note,
                                    item.specialInstruction
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

            if (nextStatus != null) {
                Button(
                    onClick = { onUpdateStatus(nextStatus) },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(nextStatus.partnerActionLabelRes()))
                }
            }

            if (order.status == OrderStatus.READY_FOR_PICKUP && order.pickupCode.isNotBlank()) {
                PickupCodeCard(
                    orderId = order.id,
                    pickupCode = order.pickupCode,
                    title = stringResource(R.string.pickup_code_title),
                    subtitle = stringResource(R.string.pickup_code_partner_subtitle),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun OrderStatus.nextPartnerStatus(): OrderStatus? {
    return when (this) {
        OrderStatus.PENDING -> OrderStatus.ACCEPTED
        OrderStatus.ACCEPTED -> OrderStatus.PREPARING
        OrderStatus.PREPARING -> OrderStatus.READY_FOR_PICKUP
        OrderStatus.READY_FOR_PICKUP,
        OrderStatus.PICKED_UP,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED -> null
    }
}

private fun OrderStatus.partnerActionLabelRes(): Int {
    return when (this) {
        OrderStatus.ACCEPTED -> R.string.partner_action_accept
        OrderStatus.PREPARING -> R.string.partner_action_prepare
        OrderStatus.READY_FOR_PICKUP -> R.string.partner_action_ready_for_pickup
        OrderStatus.PICKED_UP -> R.string.partner_action_picked_up
        OrderStatus.ON_THE_WAY -> R.string.delivery_action_on_the_way
        OrderStatus.DELIVERED -> R.string.delivery_action_delivered
        else -> R.string.update
    }
}
