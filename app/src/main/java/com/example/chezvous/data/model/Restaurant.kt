package com.example.chezvous.data.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val cuisineType: String = "",
    val rating: Double = 0.0,
    val deliveryTime: String = "",
    val minimumOrder: Double = 0.0,
    val imageUrl: String = "",
    val isOpen: Boolean = true
)
