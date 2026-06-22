package com.example.chezvous.data.repository

import com.example.chezvous.data.model.AppNotification
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val registration = firestore
            .collection(FirestoreCollections.NOTIFICATIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AppNotification(
                        id = doc.id,
                        type = data["type"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        body = data["body"] as? String ?: "",
                        relatedUserId = data["relatedUserId"] as? String ?: "",
                        restaurantId = data["restaurantId"] as? String ?: "",
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                        isRead = data["isRead"] as? Boolean ?: false
                    )
                }
                trySend(notifications)
            }
        awaitClose { registration.remove() }
    }.catch { emit(emptyList()) }

    suspend fun markAsRead(notificationId: String) {
        runCatching {
            firestore
                .collection(FirestoreCollections.NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .await()
        }
    }

    suspend fun pushNotification(notification: AppNotification): Result<Unit> = runCatching {
        val doc = firestore.collection(FirestoreCollections.NOTIFICATIONS).document()
        doc.set(
            mapOf(
                "id" to doc.id,
                "type" to notification.type,
                "title" to notification.title,
                "body" to notification.body,
                "relatedUserId" to notification.relatedUserId,
                "restaurantId" to notification.restaurantId,
                "createdAt" to notification.createdAt,
                "isRead" to notification.isRead
            )
        ).await()
    }

    fun observeRestaurantNotifications(restaurantId: String): Flow<List<AppNotification>> = callbackFlow {
        val registration = firestore
            .collection(FirestoreCollections.NOTIFICATIONS)
            .whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AppNotification(
                        id = doc.id,
                        type = data["type"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        body = data["body"] as? String ?: "",
                        relatedUserId = data["relatedUserId"] as? String ?: "",
                        restaurantId = data["restaurantId"] as? String ?: "",
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                        isRead = data["isRead"] as? Boolean ?: false
                    )
                }.sortedByDescending { it.createdAt }
                trySend(notifications)
            }
        awaitClose { registration.remove() }
    }.catch { emit(emptyList()) }
}
