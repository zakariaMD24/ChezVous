package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.example.chezvous.R
import com.example.chezvous.data.model.CartItem
import com.example.chezvous.ui.theme.ChezVousSize
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onInstructionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cartItem.foodItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(ChezVousSpacing.xxs))

                    Text(
                        text = cartItem.foodItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            CartCustomizationSummary(cartItem = cartItem)

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            ChezVousTextField(
                value = cartItem.specialInstruction,
                onValueChange = onInstructionChange,
                label = stringResource(R.string.item_instruction_label),
                leadingIcon = Icons.Outlined.EditNote,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = cartItem.unitPrice.asDhPrice(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = cartItem.totalPrice.asDhPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                QuantityControl(
                    quantity = cartItem.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }
        }
    }
}

@Composable
private fun CartCustomizationSummary(cartItem: CartItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
    ) {
        if (cartItem.selectedExtras.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.extras_summary,
                    cartItem.selectedExtras.joinToString { it.name }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (cartItem.removedIngredients.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.removed_ingredients_summary,
                    cartItem.removedIngredients.joinToString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (cartItem.spiceLevel.isNotBlank()) {
            Text(
                text = stringResource(R.string.spice_summary, cartItem.spiceLevel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.xxs,
                vertical = ChezVousSpacing.xxs
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(ChezVousSize.buttonHeight - ChezVousSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = stringResource(R.string.decrease)
                )
            }

            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(ChezVousSpacing.xxs))

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(ChezVousSize.buttonHeight - ChezVousSpacing.md)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.increase)
                )
            }
        }
    }
}
