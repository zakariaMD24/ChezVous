package com.example.chezvous.data.repository

import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val DEFAULT_DELIVERY_FEE = 10.0

sealed class CartActionResult {
    data class Success(val message: String) : CartActionResult()
    data class Error(val message: String) : CartActionResult()
}

object CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _restaurant = MutableStateFlow<Restaurant?>(null)
    val restaurant: StateFlow<Restaurant?> = _restaurant.asStateFlow()

    fun addItem(
        foodItem: FoodItem,
        restaurant: Restaurant,
        selectedExtras: List<CustomizationOption> = emptyList(),
        removedIngredients: List<String> = emptyList(),
        spiceLevel: String = "",
        specialInstruction: String = ""
    ): CartActionResult {
        if (!foodItem.isAvailable) {
            return CartActionResult.Error("Ce plat n'est pas disponible.")
        }

        val normalizedInstruction = specialInstruction.trim().take(120)
        val currentRestaurant = _restaurant.value
        if (_cartItems.value.isNotEmpty() && currentRestaurant?.id != restaurant.id) {
            return CartActionResult.Error(
                "Votre panier contient deja des plats de ${currentRestaurant?.name ?: "un autre restaurant"}."
            )
        }

        _restaurant.value = restaurant
        val lineId = buildCartLineId(
            foodItem = foodItem,
            selectedExtras = selectedExtras,
            removedIngredients = removedIngredients,
            spiceLevel = spiceLevel,
            specialInstruction = normalizedInstruction
        )
        val existingItem = _cartItems.value.firstOrNull { it.lineId == lineId }
        _cartItems.value = if (existingItem == null) {
            _cartItems.value + CartItem(
                lineId = lineId,
                foodItem = foodItem,
                quantity = 1,
                selectedExtras = selectedExtras,
                removedIngredients = removedIngredients,
                spiceLevel = spiceLevel,
                specialInstruction = normalizedInstruction
            )
        } else {
            _cartItems.value.map { item ->
                if (item.lineId == lineId) {
                    item.copy(quantity = item.quantity + 1)
                } else {
                    item
                }
            }
        }

        return CartActionResult.Success("${foodItem.name} ajoute au panier.")
    }

    fun increaseQuantity(lineId: String) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.matchesLine(lineId)) {
                item.copy(quantity = item.quantity + 1)
            } else {
                item
            }
        }
    }

    fun decreaseQuantity(lineId: String) {
        _cartItems.value = _cartItems.value.mapNotNull { item ->
            when {
                !item.matchesLine(lineId) -> item
                item.quantity <= 1 -> null
                else -> item.copy(quantity = item.quantity - 1)
            }
        }
        clearRestaurantIfEmpty()
    }

    fun removeItem(lineId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.matchesLine(lineId) }
        clearRestaurantIfEmpty()
    }

    fun updateSpecialInstruction(lineId: String, instruction: String) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.matchesLine(lineId)) {
                item.copy(specialInstruction = instruction.take(120))
            } else {
                item
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _restaurant.value = null
    }

    fun subtotal(): Double {
        return _cartItems.value.sumOf { it.totalPrice }
    }

    fun deliveryFee(): Double {
        return if (_cartItems.value.isEmpty()) 0.0 else DEFAULT_DELIVERY_FEE
    }

    private fun clearRestaurantIfEmpty() {
        if (_cartItems.value.isEmpty()) {
            _restaurant.value = null
        }
    }

    private fun CartItem.matchesLine(lineId: String): Boolean {
        return this.lineId == lineId || foodItem.id == lineId
    }

    private fun buildCartLineId(
        foodItem: FoodItem,
        selectedExtras: List<CustomizationOption>,
        removedIngredients: List<String>,
        spiceLevel: String,
        specialInstruction: String
    ): String {
        return listOf(
            foodItem.id,
            selectedExtras.map { it.id.ifBlank { it.name } }.sorted().joinToString(","),
            removedIngredients.sorted().joinToString(","),
            spiceLevel,
            specialInstruction.trim()
        ).joinToString("|")
    }
}
