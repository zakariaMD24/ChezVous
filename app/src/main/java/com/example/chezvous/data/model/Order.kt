package com.example.chezvous.data.model

data class Order(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val status: OrderStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}