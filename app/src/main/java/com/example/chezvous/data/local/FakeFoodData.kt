package com.example.chezvous.data.local

import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant

object FakeFoodData {
    val restaurants = listOf(
        Restaurant(
            id = "burger-house",
            name = "Burger House",
            cuisineType = "Fast Food",
            rating = 4.6,
            deliveryTime = "25-35 min",
            minimumOrder = 30.0
        ),
        Restaurant(
            id = "casa-pizza",
            name = "Casa Pizza",
            cuisineType = "Pizza",
            rating = 4.4,
            deliveryTime = "30-40 min",
            minimumOrder = 40.0
        ),
        Restaurant(
            id = "healthy-bowl",
            name = "Healthy Bowl",
            cuisineType = "Healthy",
            rating = 4.8,
            deliveryTime = "20-30 min",
            minimumOrder = 35.0
        )
    )

    val menuItems = listOf(
        FoodItem(
            id = "classic-burger",
            restaurantId = "burger-house",
            name = "Classic Burger",
            description = "Burger boeuf, fromage, salade et sauce maison.",
            price = 45.0,
            category = "Burgers"
        ),
        FoodItem(
            id = "chicken-burger",
            restaurantId = "burger-house",
            name = "Chicken Burger",
            description = "Poulet croustillant, cheddar et sauce ChezVous.",
            price = 42.0,
            category = "Burgers"
        ),
        FoodItem(
            id = "margherita",
            restaurantId = "casa-pizza",
            name = "Pizza Margherita",
            description = "Tomate, mozzarella et basilic.",
            price = 55.0,
            category = "Pizzas"
        ),
        FoodItem(
            id = "pepperoni",
            restaurantId = "casa-pizza",
            name = "Pizza Pepperoni",
            description = "Mozzarella, pepperoni et sauce tomate.",
            price = 68.0,
            category = "Pizzas"
        ),
        FoodItem(
            id = "salmon-bowl",
            restaurantId = "healthy-bowl",
            name = "Salmon Bowl",
            description = "Riz, saumon, avocat, legumes et sauce soja.",
            price = 72.0,
            category = "Bowls"
        ),
        FoodItem(
            id = "veggie-bowl",
            restaurantId = "healthy-bowl",
            name = "Veggie Bowl",
            description = "Quinoa, pois chiches, avocat et legumes frais.",
            price = 58.0,
            category = "Bowls"
        )
    )

    val drivers = listOf(
        Driver(
            id = "driver-yassine",
            fullName = "Yassine El Amrani",
            phone = "+212600000001",
            rating = 4.7,
            vehicleType = "Moto"
        )
    )
}
