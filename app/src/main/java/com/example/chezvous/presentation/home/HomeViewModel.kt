package com.example.chezvous.presentation.home

import androidx.lifecycle.ViewModel
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val repository = RestaurantRepository()

    private val _restaurants = MutableStateFlow(repository.getRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants

    fun searchRestaurants(query: String) {
        val allRestaurants = repository.getRestaurants()

        _restaurants.value = if (query.isBlank()) {
            allRestaurants
        } else {
            allRestaurants.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.cuisineType.contains(query, ignoreCase = true)
            }
        }
    }
}