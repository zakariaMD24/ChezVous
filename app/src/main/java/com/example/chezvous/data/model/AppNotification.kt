package com.example.chezvous.data.model

data class AppNotification(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val relatedUserId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

object NotificationType {
    const val NEW_USER = "NEW_USER"
    const val NEW_ORDER = "NEW_ORDER"
}
