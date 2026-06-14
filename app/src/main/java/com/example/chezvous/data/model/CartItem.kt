package com.example.chezvous.data.model

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    val quantity: Int = 0
) {
    val totalPrice: Double
        get() = foodItem.price * quantity
}
