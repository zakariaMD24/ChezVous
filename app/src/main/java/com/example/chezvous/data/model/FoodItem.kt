package com.example.chezvous.data.model

data class FoodItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
)