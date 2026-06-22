package com.example.chezvous.data.repository

import com.example.chezvous.data.model.AppNotification
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeNotifications(
        userId: String,
        role: String
    ): Flow<List<AppNotification>> {
        return callbackFlow {
            if (userId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            var userNotifications: List<AppNotification>? = null
            var roleNotifications: List<AppNotification>? = if (role.isBlank()) emptyList() else null

            fun publishIfReady() {
                if (userNotifications == null || roleNotifications == null) return
                trySend(
                    (userNotifications.orEmpty() + roleNotifications.orEmpty())
                        .distinctBy { it.id }
                        .sortedByDescending { it.createdAt }
                        .take(30)
                )
            }

            val userRegistration = firestore
                .collection(FirestoreCollections.NOTIFICATIONS)
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    userNotifications = if (error != null || snapshot == null) {
                        emptyList()
                    } else {
                        snapshot.documents.mapNotNull { it.toAppNotification() }
                    }
                    publishIfReady()
                }

            val roleRegistration = if (role.isNotBlank()) {
                firestore
                    .collection(FirestoreCollections.NOTIFICATIONS)
                    .whereEqualTo("roleTarget", role)
                    .addSnapshotListener { snapshot, error ->
                        roleNotifications = if (error != null || snapshot == null) {
                            emptyList()
                        } else {
                            snapshot.documents.mapNotNull { it.toAppNotification() }
                        }
                        publishIfReady()
                    }
            } else {
                null
            }

            awaitClose {
                userRegistration.remove()
                roleRegistration?.remove()
            }
        }.catch {
            emit(emptyList())
        }
    }

    suspend fun createNotification(notification: AppNotification): Result<Unit> {
        return try {
            val document = firestore.collection(FirestoreCollections.NOTIFICATIONS).document()
            document.set(notification.copy(id = document.id).toFirestoreMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationIds: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            notificationIds.distinct().forEach { notificationId ->
                batch.update(
                    firestore.collection(FirestoreCollections.NOTIFICATIONS).document(notificationId),
                    "isRead",
                    true
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AppNotification.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "roleTarget" to roleTarget,
            "title" to title,
            "message" to message,
            "type" to type,
            "relatedOrderId" to relatedOrderId,
            "relatedRestaurantId" to relatedRestaurantId,
            "isRead" to isRead,
            "createdAt" to createdAt
        )
    }

    private fun DocumentSnapshot.toAppNotification(): AppNotification? {
        val data = data ?: return null
        return AppNotification(
            id = id,
            userId = data["userId"] as? String ?: "",
            roleTarget = data["roleTarget"] as? String ?: "",
            title = data["title"] as? String ?: "",
            message = data["message"] as? String ?: "",
            type = data["type"] as? String ?: "",
            relatedOrderId = data["relatedOrderId"] as? String ?: "",
            relatedRestaurantId = data["relatedRestaurantId"] as? String ?: "",
            isRead = data["isRead"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
