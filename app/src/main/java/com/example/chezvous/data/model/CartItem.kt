package com.example.chezvous.data.model

data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int
) {
    val totalPrice: Double
        get() = foodItem.price * quantity
}