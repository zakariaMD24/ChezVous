package com.example.chezvous.data.model

data class FoodItem(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isSpiceLevelEnabled: Boolean = false,
    val extraOptions: List<CustomizationOption> = emptyList(),
    val removableIngredients: List<String> = emptyList(),
    val spiceLevels: List<String> = emptyList(),
    val removableIngredientOptions: List<CustomizationOption> = emptyList(),
    val spiceLevelOptions: List<CustomizationOption> = emptyList()
)
