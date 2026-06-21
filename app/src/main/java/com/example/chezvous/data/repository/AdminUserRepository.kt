package com.example.chezvous.data.repository

import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.User
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class UserOrderStats(
    val total: Int = 0,
    val pending: Int = 0,
    val delivered: Int = 0,
    val cancelled: Int = 0
)

data class UserOrderSummary(
    val id: String = "",
    val restaurantName: String = "",
    val totalPrice: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = 0L
)

class AdminUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getCustomers(): Result<List<User>> = runCatching {
        firestore
            .collection(FirestoreCollections.USERS)
            .whereEqualTo("role", UserRoles.CUSTOMER)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(User::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getUser(userId: String): Result<User?> = runCatching {
        val doc = firestore
            .collection(FirestoreCollections.USERS)
            .document(userId)
            .get()
            .await()
        if (doc.exists()) doc.toObject(User::class.java)?.copy(id = doc.id) else null
    }

    suspend fun getUserOrderStats(userId: String): Result<UserOrderStats> = runCatching {
        val statuses = firestore
            .collection(FirestoreCollections.ORDERS)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("status") }

        val activeStatuses = setOf(
            OrderStatus.PENDING.name,
            OrderStatus.CONFIRMED.name,
            OrderStatus.PREPARING.name,
            OrderStatus.ON_THE_WAY.name
        )

        UserOrderStats(
            total = statuses.size,
            pending = statuses.count { it in activeStatuses },
            delivered = statuses.count { it == OrderStatus.DELIVERED.name },
            cancelled = statuses.count { it == OrderStatus.CANCELLED.name }
        )
    }

    suspend fun getUserRecentOrders(userId: String): Result<List<UserOrderSummary>> = runCatching {
        firestore
            .collection(FirestoreCollections.ORDERS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                UserOrderSummary(
                    id = doc.id,
                    restaurantName = data["restaurantName"] as? String ?: "",
                    totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                    status = runCatching {
                        OrderStatus.valueOf(data["status"] as? String ?: "")
                    }.getOrDefault(OrderStatus.PENDING),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                )
            }
    }
}
