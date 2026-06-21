package com.example.chezvous.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chezvous.data.model.AppNotification
import com.example.chezvous.data.model.NotificationType
import com.example.chezvous.ui.theme.ChezVousSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminNotificationsScreen(
    onNavigateToUser: (String) -> Unit = {},
    viewModel: AdminNotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.notifications.isEmpty() -> {
            EmptyNotificationsState()
        }

        else -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // "Mark all as read" action bar — shown only when there are unreads
                if (uiState.unreadCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = ChezVousSpacing.screenHorizontal,
                                vertical = ChezVousSpacing.xs
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.unreadCount} non lue${if (uiState.unreadCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = viewModel::markAllAsRead) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(ChezVousSpacing.xxs))
                            Text(
                                text = "Tout marquer comme lu",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    HorizontalDivider()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = ChezVousSpacing.xs)
                ) {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                if (notification.relatedUserId.isNotBlank()) {
                                    onNavigateToUser(notification.relatedUserId)
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                start = ChezVousSpacing.screenHorizontal + 40.dp + ChezVousSpacing.md
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val isUnread = !notification.isRead
    val (icon, iconBgColor, iconTintColor) = notificationVisuals(notification.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isUnread) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(
                horizontal = ChezVousSpacing.screenHorizontal,
                vertical = ChezVousSpacing.md
            ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(ChezVousSpacing.md)
    ) {
        // Circular icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Text content
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = ChezVousSpacing.xs)
                )
            }

            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnread) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Unread dot
        if (isUnread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun notificationVisuals(type: String): Triple<ImageVector, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (type) {
        NotificationType.NEW_USER -> Triple(
            Icons.Outlined.PersonAdd,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        NotificationType.NEW_ORDER -> Triple(
            Icons.AutoMirrored.Outlined.ReceiptLong,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Triple(
            Icons.Outlined.Notifications,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyNotificationsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(ChezVousSpacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ChezVousSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Aucune notification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Les nouvelles inscriptions et commandes apparaîtront ici.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L -> "À l'instant"
        diff < 3_600_000L -> "Il y a ${diff / 60_000} min"
        diff < 86_400_000L -> "Il y a ${diff / 3_600_000} h"
        else -> SimpleDateFormat("dd/MM à HH:mm", Locale.FRENCH).format(Date(timestamp))
    }
}
