package com.example.chezvous.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalPrice: Double = 0.0,
    val deliveryAddress: String = "",
    val paymentMethod: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val status: OrderStatus = OrderStatus.PENDING,
    val driverId: String = "",
    val pickupCode: String = "",
    val pickupCodeValidatedAt: Long = 0L,
    val estimatedDeliveryTime: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    CASH_ON_DELIVERY
}
