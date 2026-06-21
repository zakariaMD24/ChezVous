package com.example.chezvous.data.repository

import com.example.chezvous.data.local.FakeFoodData
import com.example.chezvous.data.model.Driver
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

    private fun Driver.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "phone" to phone,
            "rating" to rating,
            "vehicleType" to vehicleType,
            "isAvailable" to isAvailable
        )
    }
}
