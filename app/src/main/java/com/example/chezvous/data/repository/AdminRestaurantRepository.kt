package com.example.chezvous.data.repository

import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.Restaurant
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class RestaurantOrderStats(
    val total: Int = 0,
    val active: Int = 0,
    val completed: Int = 0,
    val cancelled: Int = 0,
    val revenue: Double = 0.0
)

data class RestaurantOrderSummary(
    val id: String = "",
    val totalPrice: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = 0L,
    val itemCount: Int = 0
)

class AdminRestaurantRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getRestaurants(): Result<List<Restaurant>> = runCatching {
        firestore
            .collection(FirestoreCollections.RESTAURANTS)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Restaurant::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getRestaurant(restaurantId: String): Result<Restaurant?> = runCatching {
        val doc = firestore
            .collection(FirestoreCollections.RESTAURANTS)
            .document(restaurantId)
            .get()
            .await()
        if (doc.exists()) doc.toObject(Restaurant::class.java)?.copy(id = doc.id) else null
    }

    suspend fun getRestaurantOrderStats(restaurantId: String): Result<RestaurantOrderStats> = runCatching {
        val docs = firestore
            .collection(FirestoreCollections.ORDERS)
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .await()
            .documents

        val activeStatuses = setOf(
            OrderStatus.PENDING.name,
            OrderStatus.CONFIRMED.name,
            OrderStatus.PREPARING.name,
            OrderStatus.ON_THE_WAY.name
        )

        var revenue = 0.0
        var active = 0
        var completed = 0
        var cancelled = 0

        docs.forEach { doc ->
            val data = doc.data ?: return@forEach
            val status = data["status"] as? String ?: ""
            val price = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0
            when {
                status in activeStatuses -> active++
                status == OrderStatus.DELIVERED.name -> { completed++; revenue += price }
                status == OrderStatus.CANCELLED.name -> cancelled++
            }
        }

        RestaurantOrderStats(
            total = docs.size,
            active = active,
            completed = completed,
            cancelled = cancelled,
            revenue = revenue
        )
    }

    suspend fun deleteRestaurant(restaurantId: String): Result<Unit> = runCatching {
        val menuItemDocs = firestore
            .collection(FirestoreCollections.MENU_ITEMS)
            .whereEqualTo("restaurantId", restaurantId)
            .get()
            .await()
            .documents

        val batch = firestore.batch()

        menuItemDocs.forEach { doc -> batch.delete(doc.reference) }

        batch.delete(
            firestore
                .collection(FirestoreCollections.RESTAURANTS)
                .document(restaurantId)
        )

        batch.commit().await()
    }

    suspend fun createRestaurant(restaurant: Restaurant): Result<String> = runCatching {
        val document = firestore.collection(FirestoreCollections.RESTAURANTS).document()
        val restaurantWithId = restaurant.copy(id = document.id)
        document.set(restaurantWithId).await()
        document.id
    }

    suspend fun getRestaurantRecentOrders(restaurantId: String): Result<List<RestaurantOrderSummary>> = runCatching {
        firestore
            .collection(FirestoreCollections.ORDERS)
            .whereEqualTo("restaurantId", restaurantId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val items = data["items"] as? List<*>
                RestaurantOrderSummary(
                    id = doc.id,
                    totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                    status = runCatching {
                        OrderStatus.valueOf(data["status"] as? String ?: "")
                    }.getOrDefault(OrderStatus.PENDING),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                    itemCount = items?.size ?: 0
                )
            }
    }
}
