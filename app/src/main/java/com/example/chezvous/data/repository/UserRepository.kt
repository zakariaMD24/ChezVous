package com.example.chezvous.data.repository

import com.example.chezvous.data.model.User
import com.example.chezvous.data.model.UserRoles
import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeUser(userId: String): Flow<User?> {
        return callbackFlow {
            val registration = firestore
                .collection(FirestoreCollections.USERS)
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }

                    trySend(snapshot.toObject(User::class.java)?.copy(id = snapshot.id))
                }

            awaitClose { registration.remove() }
        }.catch {
            emit(null)
        }
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore
                .collection(FirestoreCollections.USERS)
                .document(user.id)
                .set(user)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserIfMissing(user: User): Result<Unit> {
        return try {
            val userDocument = firestore
                .collection(FirestoreCollections.USERS)
                .document(user.id)

            val snapshot = userDocument.get().await()

            if (!snapshot.exists()) {
                userDocument.set(user).await()
            } else {
                val existingUser = snapshot.toObject(User::class.java)
                val missingFields = buildMap {
                    if (existingUser?.email.isNullOrBlank() && user.email.isNotBlank()) {
                        put("email", user.email)
                    }

                    if (existingUser?.fullName.isNullOrBlank() && user.fullName.isNotBlank()) {
                        put("fullName", user.fullName)
                    }

                    if (existingUser?.role.isNullOrBlank()) {
                        put("role", UserRoles.CUSTOMER)
                    }
                }

                if (missingFields.isNotEmpty()) {
                    userDocument.update(missingFields).await()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String
    ): Result<Unit> {
        return try {
            firestore
                .collection(FirestoreCollections.USERS)
                .document(userId)
                .update(
                    mapOf(
                        "fullName" to fullName,
                        "phone" to phone,
                        "address" to address
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
