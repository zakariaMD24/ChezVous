package com.example.chezvous.data.repository

import com.example.chezvous.data.local.FakeFoodData
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class RestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeRestaurants(): Flow<List<Restaurant>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.RESTAURANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(FakeFoodData.restaurants)
                        return@addSnapshotListener
                    }

                    val restaurants = snapshot.documents.mapNotNull { document ->
                        document.toObject(Restaurant::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(restaurants.ifEmpty { FakeFoodData.restaurants })
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(FakeFoodData.restaurants)
        }
    }

    fun getRestaurants(): List<Restaurant> {
        return FakeFoodData.restaurants
    }

    fun getRestaurant(restaurantId: String): Restaurant? {
        return FakeFoodData.restaurants.firstOrNull { it.id == restaurantId }
    }

    fun observeAllMenuItems(): Flow<List<FoodItem>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.MENU_ITEMS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(FakeFoodData.menuItems)
                        return@addSnapshotListener
                    }

                    val menuItems = snapshot.documents.mapNotNull { document ->
                        document.toObject(FoodItem::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(menuItems.ifEmpty { FakeFoodData.menuItems })
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(FakeFoodData.menuItems)
        }
    }

    fun getAllMenuItems(): List<FoodItem> {
        return FakeFoodData.menuItems
    }

    fun observeMenuItems(restaurantId: String): Flow<List<FoodItem>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.MENU_ITEMS)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(getMenuItems(restaurantId))
                        return@addSnapshotListener
                    }

                    val menuItems = snapshot.documents.mapNotNull { document ->
                        document.toObject(FoodItem::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(menuItems.ifEmpty { getMenuItems(restaurantId) })
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(getMenuItems(restaurantId))
        }
    }

    fun getMenuItems(restaurantId: String): List<FoodItem> {
        return FakeFoodData.menuItems.filter { it.restaurantId == restaurantId }
    }

    suspend fun seedDemoDataIfEmpty(): Result<Unit> {
        return try {
            val existingRestaurants = firestore
                .collection(FirestoreCollections.RESTAURANTS)
                .limit(1)
                .get()
                .await()

            if (!existingRestaurants.isEmpty) {
                return Result.success(Unit)
            }

            val batch = firestore.batch()

            FakeFoodData.restaurants.forEach { restaurant ->
                val document = firestore
                    .collection(FirestoreCollections.RESTAURANTS)
                    .document(restaurant.id)
                batch.set(document, restaurant, SetOptions.merge())
            }

            FakeFoodData.menuItems.forEach { item ->
                val document = firestore
                    .collection(FirestoreCollections.MENU_ITEMS)
                    .document(item.id)
                batch.set(document, item, SetOptions.merge())
            }

            FakeFoodData.drivers.forEach { driver ->
                val document = firestore
                    .collection(FirestoreCollections.DRIVERS)
                    .document(driver.id)
                batch.set(document, driver, SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
