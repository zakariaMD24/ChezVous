package com.example.chezvous.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chezvous.ui.theme.ChezVousSpacing

@Composable
fun CartSummaryCard(
    subtotal: Double,
    deliveryFee: Double,
    total: Double,
    minimumOrder: Double,
    remainingForMinimum: Double,
    canCheckout: Boolean,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ChezVousCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(ChezVousSpacing.md)
        ) {
            Text(
                text = "Resume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            SummaryRow(label = "Sous-total", value = subtotal.asDhPrice())
            SummaryRow(label = "Livraison", value = deliveryFee.asDhPrice())
            SummaryRow(
                label = "Total",
                value = total.asDhPrice(),
                isStrong = true
            )

            Spacer(modifier = Modifier.height(ChezVousSpacing.sm))

            if (!canCheckout) {
                Text(
                    text = "Ajoutez encore ${remainingForMinimum.asDhPrice()} pour atteindre la commande minimum de ${minimumOrder.asDhPrice()}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(ChezVousSpacing.sm))
            }

            ChezVousButton(
                text = "Continuer vers paiement",
                enabled = canCheckout,
                onClick = onCheckout
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isStrong: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = if (isStrong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (isStrong) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = if (isStrong) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = if (isStrong) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isStrong) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
