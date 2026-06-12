package com.example.chezvous.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisineType: String,
    val rating: Double,
    val deliveryTime: String,
    val minimumOrder: Double
)