package com.example.chezvous.data.model

data class DriverReview(
    val id: String = "",
    val orderId: String = "",
    val driverId: String = "",
    val driverUserId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
