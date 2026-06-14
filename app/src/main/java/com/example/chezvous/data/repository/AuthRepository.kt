package com.example.chezvous.data.repository

import com.example.chezvous.data.model.User
import com.example.chezvous.data.model.UserRoles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val userRepository: UserRepository = UserRepository()
) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun currentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun currentUserEmail(): String {
        return firebaseAuth.currentUser?.email.orEmpty()
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Utilisateur introuvable"))

            userRepository.saveUser(
                User(
                    id = firebaseUser.uid,
                    fullName = fullName,
                    email = firebaseUser.email ?: email,
                    role = UserRoles.CUSTOMER
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Utilisateur Google introuvable"))

            userRepository.createUserIfMissing(
                User(
                    id = firebaseUser.uid,
                    fullName = firebaseUser.displayName.orEmpty(),
                    email = firebaseUser.email.orEmpty(),
                    phone = firebaseUser.phoneNumber.orEmpty(),
                    role = UserRoles.CUSTOMER
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
