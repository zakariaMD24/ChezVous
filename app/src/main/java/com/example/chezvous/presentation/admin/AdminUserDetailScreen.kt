package com.example.chezvous.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.repository.UserOrderStats
import com.example.chezvous.data.repository.UserOrderSummary
import com.example.chezvous.ui.components.chezVousScreenPadding
import com.example.chezvous.ui.theme.ChezVousSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminUserDetailScreen(
    viewModel: AdminUserDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    uiState.errorMessage?.let { error ->
        Box(modifier = Modifier.fillMaxSize().padding(ChezVousSpacing.xl), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(ChezVousSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        return
    }

    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .chezVousScreenPadding()
            .padding(vertical = ChezVousSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.lg)
    ) {
        UserDetailHeader(
            name = user?.fullName ?: "—",
            email = user?.email ?: "—"
        )

        OrderStatsRow(stats = uiState.orderStats)

        UserContactSection(
            name = user?.fullName ?: "—",
            email = user?.email ?: "—",
            phone = user?.phone ?: ""
        )

        RecentOrdersSection(orders = uiState.recentOrders)
    }
}

@Composable
private fun UserDetailHeader(name: String, email: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xs)
    ) {
        val initial = name.firstOrNull()?.uppercase() ?: email.firstOrNull()?.uppercase() ?: "?"

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = name.ifBlank { "—" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OrderStatsRow(stats: UserOrderStats) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Order Statistics",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = "${stats.total}",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Active",
                value = "${stats.pending}",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Delivered",
                value = "${stats.delivered}",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun UserContactSection(name: String, email: String, phone: String) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        DetailInfoRow(icon = Icons.Outlined.Person, label = "Full Name", value = name.ifBlank { "—" })
        DetailInfoRow(icon = Icons.Outlined.Email, label = "Email", value = email.ifBlank { "—" })
        if (phone.isNotBlank()) {
            DetailInfoRow(icon = Icons.Outlined.Phone, label = "Phone", value = phone)
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.xxs)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun RecentOrdersSection(orders: List<UserOrderSummary>) {
    Column(verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)) {
        Text(
            text = "Recent Orders",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (orders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(ChezVousSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "No orders placed yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            orders.forEach { order ->
                OrderSummaryCard(order = order)
            }
        }

        Spacer(modifier = Modifier.height(ChezVousSpacing.md))
    }
}

@Composable
private fun OrderSummaryCard(order: UserOrderSummary) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val dateString = remember(order.createdAt) {
        if (order.createdAt > 0L) dateFormatter.format(Date(order.createdAt)) else "—"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChezVousSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.restaurantName.ifBlank { "Restaurant" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.0f DA".format(order.totalPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OrderStatusBadge(status = order.status)
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (label, containerColor, contentColor) = when (status) {
        OrderStatus.DELIVERED -> Triple(
            "Livrée",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        OrderStatus.CANCELLED -> Triple(
            "Annulée",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        OrderStatus.ON_THE_WAY -> Triple(
            "En livraison",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        OrderStatus.PREPARING -> Triple(
            "En préparation",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        OrderStatus.CONFIRMED -> Triple(
            "Confirmée",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        OrderStatus.PENDING -> Triple(
            "En attente",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = ChezVousSpacing.xs,
                vertical = ChezVousSpacing.xxs
            ),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
