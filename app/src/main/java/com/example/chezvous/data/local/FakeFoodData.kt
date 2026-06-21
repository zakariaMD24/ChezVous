package com.example.chezvous.data.local

import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant

object FakeFoodData {
    private const val BURGER_IMAGE =
        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=900&q=80"
    private const val PIZZA_IMAGE =
        "https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=900&q=80"
    private const val BOWL_IMAGE =
        "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=80"
    private const val DRINK_IMAGE =
        "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=900&q=80"
    private const val EGG_IMAGE =
        "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?auto=format&fit=crop&w=500&q=80"
    private const val CHEESE_IMAGE =
        "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?auto=format&fit=crop&w=500&q=80"
    private const val PROTEIN_IMAGE =
        "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=500&q=80"
    private const val SAUCE_IMAGE =
        "https://images.unsplash.com/photo-1472476443507-c7a5948772fc?auto=format&fit=crop&w=500&q=80"
    private const val TOMATO_IMAGE =
        "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=500&q=80"
    private const val ONION_IMAGE =
        "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=500&q=80"
    private const val SALAD_IMAGE =
        "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=500&q=80"
    private const val LEMON_IMAGE =
        "https://images.unsplash.com/photo-1587324438673-56c78a866b15?auto=format&fit=crop&w=500&q=80"
    private const val ICE_IMAGE =
        "https://images.unsplash.com/photo-1514846326710-096e4a8035e0?auto=format&fit=crop&w=500&q=80"

    private val burgerExtras = listOf(
        CustomizationOption(id = "egg", name = "Oeuf", price = 5.0, imageUrl = EGG_IMAGE),
        CustomizationOption(id = "cheese", name = "Fromage", price = 6.0, imageUrl = CHEESE_IMAGE),
        CustomizationOption(id = "protein", name = "Protein extra", price = 12.0, imageUrl = PROTEIN_IMAGE),
        CustomizationOption(id = "sauce", name = "Sauce extra", price = 3.0, imageUrl = SAUCE_IMAGE)
    )
    private val burgerRemovableOptions = listOf(
        CustomizationOption(id = "tomato", name = "Tomate", imageUrl = TOMATO_IMAGE),
        CustomizationOption(id = "onion", name = "Oignon", imageUrl = ONION_IMAGE),
        CustomizationOption(id = "sauce", name = "Sauce", imageUrl = SAUCE_IMAGE),
        CustomizationOption(id = "salad", name = "Salade", imageUrl = SALAD_IMAGE),
        CustomizationOption(id = "cheese", name = "Fromage", imageUrl = CHEESE_IMAGE)
    )
    private val burgerRemovables = burgerRemovableOptions.map { it.name }
    private val drinkExtras = listOf(
        CustomizationOption(id = "ice", name = "Glacons", price = 0.0, imageUrl = ICE_IMAGE),
        CustomizationOption(id = "lemon", name = "Citron", price = 2.0, imageUrl = LEMON_IMAGE)
    )
    private val drinkRemovableOptions = listOf(
        CustomizationOption(id = "ice", name = "Glacons", imageUrl = ICE_IMAGE),
        CustomizationOption(id = "lemon", name = "Citron", imageUrl = LEMON_IMAGE)
    )
    private val drinkRemovables = drinkRemovableOptions.map { it.name }

    val restaurants = listOf(
        Restaurant(
            id = "burger-house",
            name = "Burger House",
            cuisineType = "Fast Food",
            rating = 4.6,
            deliveryTime = "25-35 min",
            minimumOrder = 30.0,
            imageUrl = BURGER_IMAGE
        ),
        Restaurant(
            id = "casa-pizza",
            name = "Casa Pizza",
            cuisineType = "Pizza",
            rating = 4.4,
            deliveryTime = "30-40 min",
            minimumOrder = 40.0,
            imageUrl = PIZZA_IMAGE
        ),
        Restaurant(
            id = "healthy-bowl",
            name = "Healthy Bowl",
            cuisineType = "Healthy",
            rating = 4.8,
            deliveryTime = "20-30 min",
            minimumOrder = 35.0,
            imageUrl = BOWL_IMAGE
        )
    )

    val menuItems = listOf(
        FoodItem(
            id = "classic-burger",
            restaurantId = "burger-house",
            name = "Classic Burger",
            description = "Burger boeuf, fromage, salade et sauce maison.",
            price = 45.0,
            category = "Burgers",
            imageUrl = BURGER_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = burgerExtras,
            removableIngredients = burgerRemovables,
            removableIngredientOptions = burgerRemovableOptions
        ),
        FoodItem(
            id = "chicken-burger",
            restaurantId = "burger-house",
            name = "Chicken Burger",
            description = "Poulet croustillant, cheddar et sauce ChezVous.",
            price = 42.0,
            category = "Burgers",
            imageUrl = BURGER_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = burgerExtras,
            removableIngredients = burgerRemovables,
            removableIngredientOptions = burgerRemovableOptions
        ),
        FoodItem(
            id = "margherita",
            restaurantId = "casa-pizza",
            name = "Pizza Margherita",
            description = "Tomate, mozzarella et basilic.",
            price = 55.0,
            category = "Pizzas",
            imageUrl = PIZZA_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = listOf(
                CustomizationOption(id = "cheese", name = "Fromage extra", price = 8.0, imageUrl = CHEESE_IMAGE),
                CustomizationOption(id = "egg", name = "Oeuf", price = 5.0, imageUrl = EGG_IMAGE),
                CustomizationOption(id = "sauce", name = "Sauce piquante", price = 3.0, imageUrl = SAUCE_IMAGE)
            ),
            removableIngredients = listOf("Tomate", "Mozzarella", "Basilic"),
            removableIngredientOptions = listOf(
                CustomizationOption(id = "tomato", name = "Tomate", imageUrl = TOMATO_IMAGE),
                CustomizationOption(id = "mozzarella", name = "Mozzarella", imageUrl = CHEESE_IMAGE),
                CustomizationOption(id = "basil", name = "Basilic", imageUrl = SALAD_IMAGE)
            )
        ),
        FoodItem(
            id = "pepperoni",
            restaurantId = "casa-pizza",
            name = "Pizza Pepperoni",
            description = "Mozzarella, pepperoni et sauce tomate.",
            price = 68.0,
            category = "Pizzas",
            imageUrl = PIZZA_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = listOf(
                CustomizationOption(id = "cheese", name = "Fromage extra", price = 8.0, imageUrl = CHEESE_IMAGE),
                CustomizationOption(id = "pepperoni", name = "Pepperoni extra", price = 10.0, imageUrl = PROTEIN_IMAGE),
                CustomizationOption(id = "sauce", name = "Sauce piquante", price = 3.0, imageUrl = SAUCE_IMAGE)
            ),
            removableIngredients = listOf("Pepperoni", "Mozzarella", "Sauce tomate"),
            removableIngredientOptions = listOf(
                CustomizationOption(id = "pepperoni", name = "Pepperoni", imageUrl = PROTEIN_IMAGE),
                CustomizationOption(id = "mozzarella", name = "Mozzarella", imageUrl = CHEESE_IMAGE),
                CustomizationOption(id = "tomato-sauce", name = "Sauce tomate", imageUrl = SAUCE_IMAGE)
            )
        ),
        FoodItem(
            id = "salmon-bowl",
            restaurantId = "healthy-bowl",
            name = "Salmon Bowl",
            description = "Riz, saumon, avocat, legumes et sauce soja.",
            price = 72.0,
            category = "Bowls",
            imageUrl = BOWL_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = listOf(
                CustomizationOption(id = "egg", name = "Oeuf", price = 5.0, imageUrl = EGG_IMAGE),
                CustomizationOption(id = "salmon", name = "Saumon extra", price = 18.0, imageUrl = PROTEIN_IMAGE),
                CustomizationOption(id = "avocado", name = "Avocat extra", price = 8.0, imageUrl = BOWL_IMAGE)
            ),
            removableIngredients = listOf("Avocat", "Legumes", "Sauce soja"),
            removableIngredientOptions = listOf(
                CustomizationOption(id = "avocado", name = "Avocat", imageUrl = BOWL_IMAGE),
                CustomizationOption(id = "vegetables", name = "Legumes", imageUrl = SALAD_IMAGE),
                CustomizationOption(id = "soy-sauce", name = "Sauce soja", imageUrl = SAUCE_IMAGE)
            )
        ),
        FoodItem(
            id = "veggie-bowl",
            restaurantId = "healthy-bowl",
            name = "Veggie Bowl",
            description = "Quinoa, pois chiches, avocat et legumes frais.",
            price = 58.0,
            category = "Bowls",
            imageUrl = BOWL_IMAGE,
            isSpiceLevelEnabled = true,
            extraOptions = listOf(
                CustomizationOption(id = "egg", name = "Oeuf", price = 5.0, imageUrl = EGG_IMAGE),
                CustomizationOption(id = "chickpeas", name = "Pois chiches extra", price = 6.0, imageUrl = PROTEIN_IMAGE),
                CustomizationOption(id = "avocado", name = "Avocat extra", price = 8.0, imageUrl = BOWL_IMAGE)
            ),
            removableIngredients = listOf("Avocat", "Pois chiches", "Legumes"),
            removableIngredientOptions = listOf(
                CustomizationOption(id = "avocado", name = "Avocat", imageUrl = BOWL_IMAGE),
                CustomizationOption(id = "chickpeas", name = "Pois chiches", imageUrl = PROTEIN_IMAGE),
                CustomizationOption(id = "vegetables", name = "Legumes", imageUrl = SALAD_IMAGE)
            )
        ),
        FoodItem(
            id = "coca-cola",
            restaurantId = "burger-house",
            name = "Coca-Cola",
            description = "Boisson fraiche 33cl.",
            price = 10.0,
            category = "Boissons",
            imageUrl = DRINK_IMAGE,
            extraOptions = drinkExtras,
            removableIngredients = drinkRemovables,
            removableIngredientOptions = drinkRemovableOptions
        ),
        FoodItem(
            id = "eau-minerale",
            restaurantId = "healthy-bowl",
            name = "Eau minerale",
            description = "Bouteille d'eau 50cl.",
            price = 8.0,
            category = "Boissons",
            imageUrl = DRINK_IMAGE,
            extraOptions = drinkExtras,
            removableIngredients = drinkRemovables,
            removableIngredientOptions = drinkRemovableOptions
        ),
        FoodItem(
            id = "sprite",
            restaurantId = "casa-pizza",
            name = "Sprite",
            description = "Boisson gazeuse 33cl.",
            price = 10.0,
            category = "Boissons",
            imageUrl = DRINK_IMAGE,
            extraOptions = drinkExtras,
            removableIngredients = drinkRemovables,
            removableIngredientOptions = drinkRemovableOptions
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
