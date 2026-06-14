package com.example.chezvous.data.repository

import com.example.chezvous.data.model.CartItem
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

    fun addItem(foodItem: FoodItem, restaurant: Restaurant): CartActionResult {
        if (!foodItem.isAvailable) {
            return CartActionResult.Error("Ce plat n'est pas disponible.")
        }

        val currentRestaurant = _restaurant.value
        if (_cartItems.value.isNotEmpty() && currentRestaurant?.id != restaurant.id) {
            return CartActionResult.Error(
                "Votre panier contient deja des plats de ${currentRestaurant?.name ?: "un autre restaurant"}."
            )
        }

        _restaurant.value = restaurant
        val existingItem = _cartItems.value.firstOrNull { it.foodItem.id == foodItem.id }
        _cartItems.value = if (existingItem == null) {
            _cartItems.value + CartItem(foodItem = foodItem, quantity = 1)
        } else {
            _cartItems.value.map { item ->
                if (item.foodItem.id == foodItem.id) {
                    item.copy(quantity = item.quantity + 1)
                } else {
                    item
                }
            }
        }

        return CartActionResult.Success("${foodItem.name} ajoute au panier.")
    }

    fun increaseQuantity(foodItemId: String) {
        _cartItems.value = _cartItems.value.map { item ->
            if (item.foodItem.id == foodItemId) {
                item.copy(quantity = item.quantity + 1)
            } else {
                item
            }
        }
    }

    fun decreaseQuantity(foodItemId: String) {
        _cartItems.value = _cartItems.value.mapNotNull { item ->
            when {
                item.foodItem.id != foodItemId -> item
                item.quantity <= 1 -> null
                else -> item.copy(quantity = item.quantity - 1)
            }
        }
        clearRestaurantIfEmpty()
    }

    fun removeItem(foodItemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.foodItem.id == foodItemId }
        clearRestaurantIfEmpty()
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
}
