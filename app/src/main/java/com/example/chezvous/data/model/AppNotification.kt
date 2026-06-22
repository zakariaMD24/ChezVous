package com.example.chezvous.data.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val roleTarget: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val relatedOrderId: String = "",
    val relatedRestaurantId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
