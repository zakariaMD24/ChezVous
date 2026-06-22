package com.example.chezvous.data.repository

import com.example.chezvous.data.model.CartItem
import com.example.chezvous.data.model.CustomizationOption
import com.example.chezvous.data.model.FoodItem
import com.example.chezvous.data.model.AppNotification
import com.example.chezvous.data.model.Order
import com.example.chezvous.data.model.OrderStatus
import com.example.chezvous.data.model.PaymentStatus
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notificationRepository = NotificationRepository(firestore)

    private companion object {
        val fallbackOrders = MutableStateFlow<List<Order>>(emptyList())
        val readyForPickupStatusValues = listOf(
            OrderStatus.READY_FOR_PICKUP.name,
            "Ready for pickup",
            "ready_for_pickup",
            "READY_FOR_PICK_UP",
            "READY_FOR_DELIVERY",
            "READY"
        )
    }

    fun observeUserOrders(userId: String): Flow<List<Order>> {
        return callbackFlow {
            var userIdOrders = emptyList<Order>()
            var legacyCustomerIdOrders = emptyList<Order>()

            fun publishOrders() {
                trySend(
                    (userIdOrders + legacyCustomerIdOrders)
                        .distinctBy { it.id }
                        .filter { it.userId == userId }
                        .sortedByDescending { it.createdAt }
                )
            }

            val userIdRegistration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(
                            fallbackOrders.value
                                .filter { it.userId == userId }
                                .sortedByDescending { it.createdAt }
                        )
                        return@addSnapshotListener
                    }

                    userIdOrders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                    publishOrders()
                }

            val legacyCustomerIdRegistration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("customerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        publishOrders()
                        return@addSnapshotListener
                    }

                    legacyCustomerIdOrders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                    publishOrders()
                }

            awaitClose {
                userIdRegistration.remove()
                legacyCustomerIdRegistration.remove()
            }
        }.catch {
            emit(
                fallbackOrders.value
                    .filter { it.userId == userId }
                    .sortedByDescending { it.createdAt }
            )
        }
    }

    fun observeRestaurantOrders(restaurantId: String): Flow<List<Order>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(
                            fallbackOrders.value
                                .filter { it.restaurantId == restaurantId }
                                .sortedByDescending { it.createdAt }
                        )
                        return@addSnapshotListener
                    }

                    val orders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                        .sortedByDescending { it.createdAt }
                    trySend(orders)
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(
                fallbackOrders.value
                    .filter { it.restaurantId == restaurantId }
                    .sortedByDescending { it.createdAt }
            )
        }
    }

    fun observeDriverOrders(driverId: String): Flow<List<Order>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("driverId", driverId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(
                            fallbackOrders.value
                                .filter { it.driverId == driverId }
                                .sortedByDescending { it.createdAt }
                        )
                        return@addSnapshotListener
                    }

                    val orders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                        .sortedByDescending { it.createdAt }
                    trySend(orders)
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(
                fallbackOrders.value
                    .filter { it.driverId == driverId }
                    .sortedByDescending { it.createdAt }
            )
        }
    }

    fun observeReadyForPickupOrders(): Flow<List<Order>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereIn("status", readyForPickupStatusValues)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(
                            fallbackOrders.value
                                .filter { it.status == OrderStatus.READY_FOR_PICKUP }
                                .sortedByDescending { it.createdAt }
                        )
                        return@addSnapshotListener
                    }

                    val orders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                        .sortedByDescending { it.createdAt }
                    trySend(orders)
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(
                fallbackOrders.value
                    .filter { it.status == OrderStatus.READY_FOR_PICKUP }
                    .sortedByDescending { it.createdAt }
            )
        }
    }

    fun observeDriverVisibleOrders(
        currentUserId: String,
        driverProfileId: String
    ): Flow<List<Order>> {
        return callbackFlow {
            val driverKeys = setOf(currentUserId.trim(), driverProfileId.trim())
                .filter { it.isNotBlank() }
                .toSet()

            if (driverKeys.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            var readyOrders: List<Order>? = null
            var profileAssignedOrders: List<Order>? = null
            var userAssignedOrders: List<Order>? = if (currentUserId == driverProfileId) {
                emptyList()
            } else {
                null
            }

            fun mergedVisibleOrders(): List<Order> {
                val availableReadyOrders = readyOrders.orEmpty()
                    .filter { order -> order.isVisibleToDriver(driverKeys) }
                val assignedOrders = profileAssignedOrders.orEmpty() + userAssignedOrders.orEmpty()
                return (availableReadyOrders + assignedOrders)
                    .filter { order -> order.isVisibleToDriver(driverKeys) }
                    .distinctBy { it.id }
                    .sortedWith(
                        compareByDescending<Order> { it.status != OrderStatus.DELIVERED }
                            .thenByDescending { it.createdAt }
                    )
            }

            fun publishIfReady() {
                if (readyOrders == null || profileAssignedOrders == null || userAssignedOrders == null) {
                    return
                }
                trySend(mergedVisibleOrders())
            }

            val readyRegistration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereIn("status", readyForPickupStatusValues)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        if (readyOrders == null) {
                            readyOrders = fallbackOrders.value
                                .filter { it.status == OrderStatus.READY_FOR_PICKUP }
                        }
                        publishIfReady()
                        return@addSnapshotListener
                    }

                    readyOrders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                    publishIfReady()
                }

            val profileAssignedRegistration = firestore
                .collection(FirestoreCollections.ORDERS)
                .whereEqualTo("driverId", driverProfileId.trim())
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        if (profileAssignedOrders == null) {
                            profileAssignedOrders = fallbackOrders.value
                                .filter { it.driverId.trim() == driverProfileId.trim() }
                        }
                        publishIfReady()
                        return@addSnapshotListener
                    }

                    profileAssignedOrders = snapshot.documents
                        .mapNotNull { it.toOrder() }
                    publishIfReady()
                }

            val userAssignedRegistration = if (currentUserId != driverProfileId) {
                firestore
                    .collection(FirestoreCollections.ORDERS)
                    .whereEqualTo("driverId", currentUserId.trim())
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) {
                            if (userAssignedOrders == null) {
                                userAssignedOrders = fallbackOrders.value
                                    .filter { it.driverId.trim() == currentUserId.trim() }
                            }
                            publishIfReady()
                            return@addSnapshotListener
                        }

                        userAssignedOrders = snapshot.documents
                            .mapNotNull { it.toOrder() }
                        publishIfReady()
                    }
            } else {
                null
            }

            awaitClose {
                readyRegistration.remove()
                profileAssignedRegistration.remove()
                userAssignedRegistration?.remove()
            }
        }.catch {
            val driverKeys = setOf(currentUserId.trim(), driverProfileId.trim())
                .filter { key -> key.isNotBlank() }
                .toSet()
            emit(
                fallbackOrders.value
                    .filter { order -> order.isVisibleToDriver(driverKeys) }
                    .sortedByDescending { order -> order.createdAt }
            )
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

            val orderWithId = order.copy(
                id = document.id,
                pickupCode = order.pickupCode.ifBlank { document.id.toPickupCode() }
            )
            document.set(orderWithId.toFirestoreMap()).await()
            createOrderCreatedNotifications(orderWithId)

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: OrderStatus
    ): Result<Unit> {
        return try {
            val document = firestore
                .collection(FirestoreCollections.ORDERS)
                .document(orderId)
            document.update("status", status.name).await()
            document.get().await().toOrder()?.copy(status = status)?.let { order ->
                createOrderStatusNotifications(order, status)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validatePickup(
        orderId: String,
        pickupCode: String,
        driverId: String,
        currentUserId: String = ""
    ): Result<Unit> {
        return try {
            val document = firestore
                .collection(FirestoreCollections.ORDERS)
                .document(orderId)
            val cleanCode = pickupCode.trim().uppercase()
            val cleanDriverId = driverId.trim()
            val driverKeys = setOf(cleanDriverId, currentUserId.trim())
                .filter { it.isNotBlank() }
                .toSet()

            if (cleanDriverId.isBlank()) {
                return Result.failure(IllegalArgumentException("Livreur introuvable."))
            }

            firestore.runTransaction { transaction ->
                val order = transaction.get(document).toOrder()
                    ?: throw IllegalArgumentException("Commande introuvable.")

                if (order.status != OrderStatus.READY_FOR_PICKUP) {
                    throw IllegalArgumentException("La commande n'est pas prete.")
                }

                if (order.driverId.isNotBlank() && order.driverId.trim() !in driverKeys) {
                    throw IllegalArgumentException("Cette commande est assignee a un autre livreur.")
                }

                if (order.pickupCode.uppercase() != cleanCode) {
                    throw IllegalArgumentException("Code de retrait incorrect.")
                }

                transaction.update(
                    document,
                    mapOf(
                        "status" to OrderStatus.PICKED_UP.name,
                        "driverId" to cleanDriverId,
                        "driverProfileId" to cleanDriverId,
                        "driverUserId" to currentUserId.trim(),
                        "pickupCodeValidation" to cleanCode,
                        "pickupCodeValidatedAt" to System.currentTimeMillis(),
                        "pickedUpAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }.await()
            document.get().await().toOrder()?.copy(
                status = OrderStatus.PICKED_UP,
                driverId = cleanDriverId,
                driverProfileId = cleanDriverId,
                driverUserId = currentUserId.trim()
            )?.let { order ->
                createDriverProgressNotification(order, OrderStatus.PICKED_UP)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDriverOrderStatus(
        orderId: String,
        driverId: String,
        status: OrderStatus,
        currentUserId: String = ""
    ): Result<Unit> {
        return try {
            val document = firestore
                .collection(FirestoreCollections.ORDERS)
                .document(orderId)
            val cleanDriverId = driverId.trim()
            val driverKeys = setOf(cleanDriverId, currentUserId.trim())
                .filter { it.isNotBlank() }
                .toSet()

            if (cleanDriverId.isBlank()) {
                return Result.failure(IllegalArgumentException("Livreur introuvable."))
            }

            firestore.runTransaction { transaction ->
                val order = transaction.get(document).toOrder()
                    ?: throw IllegalArgumentException("Commande introuvable.")

                if (order.driverId.trim() !in driverKeys) {
                    throw IllegalArgumentException("Cette commande n'est pas assignee a ce livreur.")
                }

                val transitionIsAllowed =
                    (order.status == OrderStatus.PICKED_UP && status == OrderStatus.ON_THE_WAY) ||
                            (order.status == OrderStatus.ON_THE_WAY && status == OrderStatus.DELIVERED)

                if (!transitionIsAllowed) {
                    throw IllegalArgumentException("Changement de statut non autorise.")
                }

                transaction.update(
                    document,
                    mapOf(
                        "status" to status.name,
                        "driverId" to cleanDriverId,
                        "driverProfileId" to cleanDriverId,
                        "driverUserId" to currentUserId.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    ) + if (status == OrderStatus.DELIVERED) {
                        mapOf("deliveredAt" to System.currentTimeMillis())
                    } else {
                        emptyMap()
                    }
                )
            }.await()
            document.get().await().toOrder()?.copy(
                status = status,
                driverId = cleanDriverId,
                driverProfileId = cleanDriverId,
                driverUserId = currentUserId.trim()
            )?.let { order ->
                createDriverProgressNotification(order, status)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Order.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "customerId" to userId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "restaurantId" to restaurantId,
            "restaurantName" to restaurantName,
            "items" to items.map { it.toFirestoreMap() },
            "subtotal" to subtotal,
            "deliveryFee" to deliveryFee,
            "totalPrice" to totalPrice,
            "total" to totalPrice,
            "deliveryAddress" to deliveryAddress,
            "deliveryNote" to deliveryNote,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus.name,
            "status" to status.name,
            "driverId" to driverId,
            "driverUserId" to driverUserId,
            "driverProfileId" to driverProfileId,
            "pickupCode" to pickupCode,
            "pickupCodeValidatedAt" to pickupCodeValidatedAt,
            "estimatedDeliveryTime" to estimatedDeliveryTime,
            "updatedAt" to updatedAt,
            "pickedUpAt" to pickedUpAt,
            "deliveredAt" to deliveredAt,
            "createdAt" to createdAt
        )
    }

    private fun CartItem.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "lineId" to lineId,
            "foodItem" to foodItem.toFirestoreMap(),
            "quantity" to quantity,
            "unitPrice" to unitPrice,
            "selectedExtras" to selectedExtras.map { it.toFirestoreMap() },
            "removedIngredients" to removedIngredients,
            "spiceLevel" to spiceLevel,
            "specialInstruction" to specialInstruction,
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
            "isAvailable" to isAvailable,
            "isSpiceLevelEnabled" to isSpiceLevelEnabled,
            "extraOptions" to extraOptions.map { it.toFirestoreMap() },
            "removableIngredients" to removableIngredients,
            "spiceLevels" to spiceLevels,
            "removableIngredientOptions" to removableIngredientOptions.map { it.toFirestoreMap() },
            "spiceLevelOptions" to spiceLevelOptions.map { it.toFirestoreMap() }
        )
    }

    private fun CustomizationOption.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "price" to price,
            "imageUrl" to imageUrl,
            "description" to description
        )
    }

    private fun DocumentSnapshot.toOrder(): Order? {
        val snapshotData = data ?: return null
        val totalPrice = snapshotData.doubleValue("totalPrice")
            .takeIf { it > 0.0 }
            ?: snapshotData.doubleValue("total")
        return Order(
            id = id,
            userId = snapshotData.stringValue("userId")
                .ifBlank { snapshotData.stringValue("customerId") },
            customerName = snapshotData.stringValue("customerName"),
            customerPhone = snapshotData.stringValue("customerPhone"),
            restaurantId = snapshotData.stringValue("restaurantId"),
            restaurantName = snapshotData.stringValue("restaurantName"),
            items = snapshotData.cartItemsValue("items"),
            subtotal = snapshotData.doubleValue("subtotal")
                .takeIf { it > 0.0 }
                ?: totalPrice,
            deliveryFee = snapshotData.doubleValue("deliveryFee"),
            totalPrice = totalPrice,
            deliveryAddress = snapshotData.stringValue("deliveryAddress"),
            deliveryNote = snapshotData.stringValue("deliveryNote"),
            paymentMethod = snapshotData.stringValue("paymentMethod"),
            paymentStatus = snapshotData.paymentStatusValue("paymentStatus"),
            status = snapshotData.orderStatusValue("status"),
            driverId = snapshotData.stringValue("driverId"),
            driverUserId = snapshotData.stringValue("driverUserId"),
            driverProfileId = snapshotData.stringValue("driverProfileId"),
            pickupCode = snapshotData.stringValue("pickupCode").ifBlank { id.toPickupCode() },
            pickupCodeValidatedAt = snapshotData.longValue("pickupCodeValidatedAt", 0L),
            estimatedDeliveryTime = snapshotData.stringValue("estimatedDeliveryTime"),
            updatedAt = snapshotData.longValue("updatedAt", 0L),
            pickedUpAt = snapshotData.longValue("pickedUpAt", 0L),
            deliveredAt = snapshotData.longValue("deliveredAt", 0L),
            createdAt = snapshotData.longValue("createdAt")
        )
    }

    private fun Map<*, *>.cartItemsValue(key: String): List<CartItem> {
        val rawItems = this[key] as? List<*> ?: return emptyList()

        return rawItems.mapNotNull { rawItem ->
            val itemMap = rawItem as? Map<*, *> ?: return@mapNotNull null
            val foodMap = itemMap["foodItem"] as? Map<*, *> ?: return@mapNotNull null

            CartItem(
                lineId = itemMap.stringValue("lineId"),
                foodItem = FoodItem(
                    id = foodMap.stringValue("id"),
                    restaurantId = foodMap.stringValue("restaurantId"),
                    name = foodMap.stringValue("name"),
                    description = foodMap.stringValue("description"),
                    price = foodMap.doubleValue("price"),
                    category = foodMap.stringValue("category"),
                    imageUrl = foodMap.stringValue("imageUrl"),
                    isAvailable = foodMap.booleanValue("isAvailable", true),
                    isSpiceLevelEnabled = foodMap.booleanValue("isSpiceLevelEnabled", false),
                    extraOptions = foodMap.customizationOptionsValue("extraOptions"),
                    removableIngredients = foodMap.stringListValue("removableIngredients"),
                    spiceLevels = foodMap.stringListValue("spiceLevels"),
                    removableIngredientOptions = foodMap.customizationOptionsValue(
                        "removableIngredientOptions"
                    ),
                    spiceLevelOptions = foodMap.customizationOptionsValue("spiceLevelOptions")
                ),
                quantity = itemMap.intValue("quantity"),
                selectedExtras = itemMap.customizationOptionsValue("selectedExtras"),
                removedIngredients = itemMap.stringListValue("removedIngredients"),
                spiceLevel = itemMap.stringValue("spiceLevel").toSafeSpiceLevel(),
                specialInstruction = itemMap.stringValue("specialInstruction")
            )
        }
    }

    private fun Map<*, *>.orderStatusValue(key: String): OrderStatus {
        val rawStatus = stringValue(key)
            .trim()
            .uppercase()
            .replace("-", "_")
            .replace(" ", "_")

        return when (rawStatus) {
            "CONFIRMED" -> OrderStatus.ACCEPTED
            "READY",
            "READY_FOR_DELIVERY",
            "READY_FOR_PICK_UP" -> OrderStatus.READY_FOR_PICKUP
            else -> runCatching {
                OrderStatus.valueOf(rawStatus)
            }.getOrDefault(OrderStatus.PENDING)
        }
    }

    private fun Map<*, *>.paymentStatusValue(key: String): PaymentStatus {
        return when (val rawStatus = stringValue(key)) {
            "PAID" -> PaymentStatus.PAID_SIMULATED
            "CASH_ON_DELIVERY" -> PaymentStatus.PENDING_CASH
            else -> runCatching {
                PaymentStatus.valueOf(rawStatus)
            }.getOrDefault(PaymentStatus.PENDING)
        }
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

    private fun Map<*, *>.longValue(
        key: String,
        defaultValue: Long
    ): Long {
        return (this[key] as? Number)?.toLong() ?: defaultValue
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

    private fun Map<*, *>.stringListValue(key: String): List<String> {
        return (this[key] as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()
    }

    private fun Map<*, *>.customizationOptionsValue(key: String): List<CustomizationOption> {
        return (this[key] as? List<*>)
            ?.mapNotNull { rawOption ->
                val optionMap = rawOption as? Map<*, *> ?: return@mapNotNull null
                CustomizationOption(
                    id = optionMap.stringValue("id"),
                    name = optionMap.stringValue("name"),
                    price = optionMap.doubleValue("price"),
                    imageUrl = optionMap.stringValue("imageUrl"),
                    description = optionMap.stringValue("description")
                )
            }
            .orEmpty()
    }

    private fun String.toPickupCode(): String {
        return takeLast(6)
            .uppercase()
            .padStart(6, '0')
    }

    private fun String.toSafeSpiceLevel(): String {
        return when (lowercase()) {
            "mild", "doux" -> "mild"
            "medium", "moyen", "normal" -> "medium"
            "spicy", "piquant", "hot", "very spicy", "very-spicy", "extra spicy", "extra-spicy" -> "spicy"
            else -> this
        }
    }

    private suspend fun createOrderCreatedNotifications(order: Order) {
        createNotification(
            userId = order.userId,
            title = "Commande envoyee",
            message = "Votre commande chez ${order.restaurantName} a ete creee.",
            type = "ORDER_CREATED",
            order = order
        )
        createNotification(
            roleTarget = UserRoles.PARTNER,
            title = "Nouvelle commande",
            message = "Une nouvelle commande est disponible chez ${order.restaurantName}.",
            type = "ORDER_CREATED",
            order = order
        )
        createNotification(
            roleTarget = UserRoles.ADMIN,
            title = "Nouvelle commande",
            message = "Une nouvelle commande est disponible chez ${order.restaurantName}.",
            type = "ORDER_CREATED",
            order = order
        )
    }

    private suspend fun createOrderStatusNotifications(
        order: Order,
        status: OrderStatus
    ) {
        when (status) {
            OrderStatus.ACCEPTED -> createNotification(
                userId = order.userId,
                title = "Commande acceptee",
                message = "${order.restaurantName} a accepte votre commande.",
                type = "ORDER_ACCEPTED",
                order = order
            )
            OrderStatus.PREPARING -> createNotification(
                userId = order.userId,
                title = "Preparation en cours",
                message = "Votre commande est en preparation.",
                type = "ORDER_PREPARING",
                order = order
            )
            OrderStatus.READY_FOR_PICKUP -> {
                createNotification(
                    userId = order.userId,
                    title = "Commande prete",
                    message = "Votre commande est prete pour le retrait livreur.",
                    type = "ORDER_READY_FOR_PICKUP",
                    order = order
                )
                createNotification(
                    roleTarget = UserRoles.DRIVER,
                    title = "Commande prete au retrait",
                    message = "Une livraison est disponible chez ${order.restaurantName}.",
                    type = "ORDER_READY_FOR_PICKUP",
                    order = order
                )
            }
            else -> Unit
        }
    }

    private suspend fun createDriverProgressNotification(
        order: Order,
        status: OrderStatus
    ) {
        when (status) {
            OrderStatus.PICKED_UP -> createNotification(
                userId = order.userId,
                title = "Commande recuperee",
                message = "Le livreur a recupere votre commande.",
                type = "ORDER_PICKED_UP",
                order = order
            )
            OrderStatus.ON_THE_WAY -> createNotification(
                userId = order.userId,
                title = "Commande en route",
                message = "Votre commande arrive bientot.",
                type = "ORDER_ON_THE_WAY",
                order = order
            )
            OrderStatus.DELIVERED -> {
                createNotification(
                    userId = order.userId,
                    title = "Commande livree",
                    message = "Votre commande a ete livree.",
                    type = "ORDER_DELIVERED",
                    order = order
                )
                createNotification(
                    userId = order.userId,
                    title = "Notez votre commande",
                    message = "Partagez votre avis sur ${order.restaurantName}.",
                    type = "ORDER_REVIEW_REQUEST",
                    order = order
                )
            }
            else -> Unit
        }
    }

    private suspend fun createNotification(
        userId: String = "",
        roleTarget: String = "",
        title: String,
        message: String,
        type: String,
        order: Order
    ) {
        notificationRepository.createNotification(
            AppNotification(
                userId = userId,
                roleTarget = roleTarget,
                title = title,
                message = message,
                type = type,
                relatedOrderId = order.id,
                relatedRestaurantId = order.restaurantId
            )
        )
    }

    private fun Order.isVisibleToDriver(driverKeys: Set<String>): Boolean {
        val driverIds = setOf(driverId.trim(), driverUserId.trim(), driverProfileId.trim())
            .filter { it.isNotBlank() }
            .toSet()
        val assignedToDriver = driverIds.any { it in driverKeys }
        return when (status) {
            OrderStatus.READY_FOR_PICKUP -> driverIds.isEmpty() || assignedToDriver
            OrderStatus.PICKED_UP,
            OrderStatus.ON_THE_WAY,
            OrderStatus.DELIVERED -> assignedToDriver
            OrderStatus.PENDING,
            OrderStatus.ACCEPTED,
            OrderStatus.PREPARING,
            OrderStatus.CANCELLED -> false
        }
    }
}
