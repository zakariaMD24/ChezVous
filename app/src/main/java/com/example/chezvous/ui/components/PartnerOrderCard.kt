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
    modifier: Modifier = Modifier
) {
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
                        text = "Commande #${order.id.takeLast(6)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${order.items.sumOf { it.quantity }} article(s) - ${order.totalPrice.asDhPrice()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusChip(status = order.status)
            }

            Text(
                text = order.deliveryAddress,
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(nextStatus.partnerActionLabel())
                }
            }
        }
    }
}

fun OrderStatus.nextPartnerStatus(): OrderStatus? {
    return when (this) {
        OrderStatus.PENDING -> OrderStatus.CONFIRMED
        OrderStatus.CONFIRMED -> OrderStatus.PREPARING
        OrderStatus.PREPARING -> OrderStatus.ON_THE_WAY
        OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
        OrderStatus.DELIVERED,
        OrderStatus.CANCELLED -> null
    }
}

private fun OrderStatus.partnerActionLabel(): String {
    return when (this) {
        OrderStatus.CONFIRMED -> "Confirmer"
        OrderStatus.PREPARING -> "Passer en preparation"
        OrderStatus.ON_THE_WAY -> "Marquer en route"
        OrderStatus.DELIVERED -> "Marquer livree"
        else -> customerLabel()
    }
}
