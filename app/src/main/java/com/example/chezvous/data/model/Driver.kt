package com.example.chezvous.data.model

data class Driver(
    val id: String = "",
    val fullName: String = "",
    val phone: String = "",
    val rating: Double = 0.0,
    val vehicleType: String = "",
    val isAvailable: Boolean = true
)
