package com.example.chezvous.data.repository

import com.example.chezvous.data.model.Restaurant

class RestaurantRepository {

    fun getRestaurants(): List<Restaurant> {
        return listOf(
            Restaurant(
                id = "1",
                name = "Burger House",
                cuisineType = "Fast Food",
                rating = 4.6,
                deliveryTime = "25-35 min",
                minimumOrder = 30.0
            ),
            Restaurant(
                id = "2",
                name = "Casa Pizza",
                cuisineType = "Pizza",
                rating = 4.4,
                deliveryTime = "30-40 min",
                minimumOrder = 40.0
            ),
            Restaurant(
                id = "3",
                name = "Healthy Bowl",
                cuisineType = "Healthy",
                rating = 4.8,
                deliveryTime = "20-30 min",
                minimumOrder = 35.0
            )
        )
    }
}