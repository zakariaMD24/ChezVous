package com.example.chezvous.data.repository

import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.PaymentStatus
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val fallbackOrders = MutableStateFlow<List<Order>>(emptyList())

    fun observeUserOrders(userId: String): Flow<List<Order>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(fallbackOrders.value.filter { it.userId == userId })
                        return@addSnapshotListener
                    }

                    val orders = snapshot.documents.mapNotNull { it.toOrder() }
                    trySend(orders)
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(fallbackOrders.value.filter { it.userId == userId })
        }
    }

    fun observeOrder(orderId: String): Flow<Order?> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.ORDERS)
                .document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(fallbackOrders.value.firstOrNull { it.id == orderId })
                        return@addSnapshotListener
                    }

                    trySend(snapshot.toOrder())
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(fallbackOrders.value.firstOrNull { it.id == orderId })
        }
    }

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val document = if (order.id.isBlank()) {
                firestore.collection(FirestoreCollections.ORDERS).document()
            } else {
                firestore.collection(FirestoreCollections.ORDERS).document(order.id)
            }

            val orderWithId = order.copy(id = document.id)
            document.set(orderWithId.toFirestoreMap()).await()

            Result.success(document.id)
        } catch (e: Exception) {
            val fallbackId = order.id.ifBlank { "local-${System.currentTimeMillis()}" }
            val fallbackOrder = order.copy(id = fallbackId)
            fallbackOrders.value = fallbackOrders.value + fallbackOrder
            Result.success(fallbackId)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ): Result<Unit> {
        return try {
            firestore
                .collection(FirestoreCollections.ORDERS)
                .document(orderId)
                .update("status", status.name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            fallbackOrders.value = fallbackOrders.value.map { order ->
                if (order.id == orderId) order.copy(status = status) else order
            }
            Result.success(Unit)
        }
    }

    private fun Order.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "restaurantId" to restaurantId,
            "restaurantName" to restaurantName,
            "items" to items.map { it.toFirestoreMap() },
            "subtotal" to subtotal,
            "deliveryFee" to deliveryFee,
            "totalPrice" to totalPrice,
            "deliveryAddress" to deliveryAddress,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus.name,
            "status" to status.name,
            "driverId" to driverId,
            "estimatedDeliveryTime" to estimatedDeliveryTime,
            "createdAt" to createdAt
        )
    }

    private fun CartItem.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "foodItem" to foodItem.toFirestoreMap(),
            "quantity" to quantity,
            "totalPrice" to totalPrice
        )
    }

    private fun FoodItem.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "restaurantId" to restaurantId,
            "name" to name,
            "description" to description,
            "price" to price,
            "category" to category,
            "imageUrl" to imageUrl,
            "isAvailable" to isAvailable
        )
    }

    private fun DocumentSnapshot.toOrder(): Order? {
        val snapshotData = data ?: return null
        return Order(
            id = id,
            userId = snapshotData.stringValue("userId"),
            restaurantId = snapshotData.stringValue("restaurantId"),
            restaurantName = snapshotData.stringValue("restaurantName"),
            items = snapshotData.cartItemsValue("items"),
            subtotal = snapshotData.doubleValue("subtotal"),
            deliveryFee = snapshotData.doubleValue("deliveryFee"),
            totalPrice = snapshotData.doubleValue("totalPrice"),
            deliveryAddress = snapshotData.stringValue("deliveryAddress"),
            paymentMethod = snapshotData.stringValue("paymentMethod"),
            paymentStatus = snapshotData.paymentStatusValue("paymentStatus"),
            status = snapshotData.orderStatusValue("status"),
            driverId = snapshotData.stringValue("driverId"),
            estimatedDeliveryTime = snapshotData.stringValue("estimatedDeliveryTime"),
            createdAt = snapshotData.longValue("createdAt")
        )
    }

    private fun Map<*, *>.cartItemsValue(key: String): List<CartItem> {
        val rawItems = this[key] as? List<*> ?: return emptyList()

        return rawItems.mapNotNull { rawItem ->
            val itemMap = rawItem as? Map<*, *> ?: return@mapNotNull null
            val foodMap = itemMap["foodItem"] as? Map<*, *> ?: return@mapNotNull null

            CartItem(
                foodItem = FoodItem(
                    id = foodMap.stringValue("id"),
                    restaurantId = foodMap.stringValue("restaurantId"),
                    name = foodMap.stringValue("name"),
                    description = foodMap.stringValue("description"),
                    price = foodMap.doubleValue("price"),
                    category = foodMap.stringValue("category"),
                    imageUrl = foodMap.stringValue("imageUrl"),
                    isAvailable = foodMap.booleanValue("isAvailable", true)
                ),
                quantity = itemMap.intValue("quantity")
            )
        }
    }

    private fun Map<*, *>.orderStatusValue(key: String): OrderStatus {
        return runCatching {
            OrderStatus.valueOf(stringValue(key))
        }.getOrDefault(OrderStatus.PENDING)
    }

    private fun Map<*, *>.paymentStatusValue(key: String): PaymentStatus {
        return runCatching {
            PaymentStatus.valueOf(stringValue(key))
        }.getOrDefault(PaymentStatus.PENDING)
    }

    private fun Map<*, *>.stringValue(key: String): String {
        return this[key] as? String ?: ""
    }

    private fun Map<*, *>.doubleValue(key: String): Double {
        return (this[key] as? Number)?.toDouble() ?: 0.0
    }

    private fun Map<*, *>.longValue(key: String): Long {
        return (this[key] as? Number)?.toLong() ?: System.currentTimeMillis()
    }

    private fun Map<*, *>.intValue(key: String): Int {
        return (this[key] as? Number)?.toInt() ?: 0
    }

    private fun Map<*, *>.booleanValue(
        key: String,
        defaultValue: Boolean
    ): Boolean {
        return this[key] as? Boolean ?: defaultValue
    }
}
