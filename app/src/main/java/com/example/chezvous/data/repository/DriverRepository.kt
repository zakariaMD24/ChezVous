package com.example.chezvous.data.repository

import com.example.chezvous.data.local.FakeFoodData
import com.example.chezvous.data.model.Driver
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch

class DriverRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
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
}
