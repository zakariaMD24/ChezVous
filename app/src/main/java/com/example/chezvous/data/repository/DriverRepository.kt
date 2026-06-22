package com.example.chezvous.data.repository

import com.example.chezvous.data.local.FakeFoodData
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.model.DriverReview
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class DriverRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeDrivers(): Flow<List<Driver>> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.DRIVERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(FakeFoodData.drivers)
                        return@addSnapshotListener
                    }

                    val drivers = snapshot.documents.mapNotNull { document ->
                        document.toObject(Driver::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(drivers.ifEmpty { FakeFoodData.drivers })
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(FakeFoodData.drivers)
        }
    }

    fun observeDriver(driverId: String): Flow<Driver?> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.DRIVERS)
                .document(driverId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(FakeFoodData.drivers.firstOrNull { it.id == driverId })
                        return@addSnapshotListener
                    }

                    val driver = snapshot.toObject(Driver::class.java)
                        ?.copy(id = snapshot.id)

                    trySend(driver ?: FakeFoodData.drivers.firstOrNull { it.id == driverId })
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(FakeFoodData.drivers.firstOrNull { it.id == driverId })
        }
    }

    suspend fun createDriver(driver: Driver): Result<String> {
        return try {
            val document = firestore.collection(FirestoreCollections.DRIVERS).document()
            val driverWithId = driver.copy(id = document.id)
            document.set(driverWithId.toFirestoreMap(), SetOptions.merge()).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDriver(driver: Driver): Result<Unit> {
        return try {
            firestore
                .collection(FirestoreCollections.DRIVERS)
                .document(driver.id)
                .set(driver.toFirestoreMap(), SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDriverAvailability(
        driverId: String,
        isAvailable: Boolean
    ): Result<Unit> {
        return try {
            val document = firestore
                .collection(FirestoreCollections.DRIVERS)
                .document(driverId)
            val snapshot = document.get().await()

            if (snapshot.exists()) {
                document.update("isAvailable", isAvailable).await()
            } else {
                val fallbackDriver = FakeFoodData.drivers
                    .firstOrNull { it.id == driverId }
                    ?.copy(isAvailable = isAvailable)
                    ?: Driver(id = driverId, isAvailable = isAvailable)
                document.set(fallbackDriver.toFirestoreMap(), SetOptions.merge()).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitDriverReview(review: DriverReview): Result<Unit> {
        return try {
            val cleanRating = review.rating.coerceIn(1, 5)
            val cleanDriverId = review.driverId.trim()
            val reviewId = "${review.orderId}_${review.customerId}"

            if (cleanDriverId.isBlank()) {
                return Result.failure(IllegalArgumentException("Livreur introuvable."))
            }

            val reviewDocument = firestore
                .collection(FirestoreCollections.DRIVER_REVIEWS)
                .document(reviewId)
            val driverDocument = firestore
                .collection(FirestoreCollections.DRIVERS)
                .document(cleanDriverId)

            firestore.runTransaction { transaction ->
                if (transaction.get(reviewDocument).exists()) {
                    throw IllegalArgumentException("Vous avez deja note ce livreur pour cette commande.")
                }

                val driverSnapshot = transaction.get(driverDocument)
                if (!driverSnapshot.exists()) {
                    throw IllegalArgumentException("Livreur introuvable.")
                }

                val currentCount = (driverSnapshot.get("ratingCount") as? Number)?.toInt() ?: 0
                val currentAverage = if (currentCount > 0) {
                    (driverSnapshot.get("rating") as? Number)?.toDouble() ?: 0.0
                } else {
                    0.0
                }
                val newCount = currentCount + 1
                val newAverage = ((currentAverage * currentCount) + cleanRating) / newCount

                transaction.set(
                    reviewDocument,
                    review.copy(
                        id = reviewId,
                        driverId = cleanDriverId,
                        rating = cleanRating,
                        comment = review.comment.trim()
                    )
                )
                transaction.update(
                    driverDocument,
                    mapOf(
                        "rating" to newAverage,
                        "ratingCount" to newCount
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Driver.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "phone" to phone,
            "rating" to rating,
            "ratingCount" to ratingCount,
            "vehicleType" to vehicleType,
            "isAvailable" to isAvailable
        )
    }
}
