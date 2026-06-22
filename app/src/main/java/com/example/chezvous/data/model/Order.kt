package com.example.chezvous.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalPrice: Double = 0.0,
    val deliveryAddress: String = "",
    val deliveryNote: String = "",
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
    ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    PENDING_CASH,
    PAID,
    PAID_SIMULATED,
    FAILED,
    CASH_ON_DELIVERY
}
