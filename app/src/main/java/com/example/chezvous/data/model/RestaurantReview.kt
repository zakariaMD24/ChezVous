package com.example.chezvous.data.model

data class RestaurantReview(
    val id: String = "",
    val orderId: String = "",
    val restaurantId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
