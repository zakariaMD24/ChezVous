package com.example.chezvous.data.model

data class CartItem(
    val lineId: String = "",
    val foodItem: FoodItem = FoodItem(),
    val quantity: Int = 0,
    val selectedExtras: List<CustomizationOption> = emptyList(),
    val removedIngredients: List<String> = emptyList(),
    val spiceLevel: String = "",
    val specialInstruction: String = ""
) {
    val unitPrice: Double
        get() = foodItem.price + selectedExtras.sumOf { it.price }

    val totalPrice: Double
        get() = unitPrice * quantity
}
