package com.example.chezvous.ui.components

import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem

fun FoodItem.displayFoodImageUrl(): String {
    return imageUrl.ifBlank { fallbackFoodImageUrl(name, category) }
}

fun CustomizationOption.displayCustomizationImageUrl(): String {
    return imageUrl
}

fun FoodItem.isDrinkItem(): Boolean {
    return category.contains("boisson", ignoreCase = true) ||
            category.contains("drink", ignoreCase = true) ||
            category.contains("soda", ignoreCase = true)
}

private fun fallbackFoodImageUrl(
    name: String,
    category: String
): String {
    val text = "$name $category"
    return when {
        text.contains("pizza", ignoreCase = true) ->
            "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=900&q=80"
        text.contains("burger", ignoreCase = true) ->
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80"
        text.contains("boisson", ignoreCase = true) ||
                text.contains("drink", ignoreCase = true) ||
                text.contains("soda", ignoreCase = true) ||
                text.contains("coca", ignoreCase = true) ||
                text.contains("sprite", ignoreCase = true) ->
            "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=900&q=80"
        text.contains("dessert", ignoreCase = true) ||
                text.contains("cake", ignoreCase = true) ->
            "https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80"
        else ->
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=80"
    }
}
