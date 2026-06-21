package com.example.chezvous.data.repository

import com.example.chezvous.data.remote.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getUserCount(): Result<Int> = runCatching {
        firestore
            .collection(FirestoreCollections.USERS)
            .get()
            .await()
            .size()
    }

    suspend fun getRestaurantCount(): Result<Int> = runCatching {
        firestore
            .collection(FirestoreCollections.RESTAURANTS)
            .get()
            .await()
            .size()
    }

    suspend fun getOrderCount(): Result<Int> = runCatching {
        firestore
            .collection(FirestoreCollections.ORDERS)
            .get()
            .await()
            .size()
    }

    suspend fun getTodayOrderCount(): Result<Int> = runCatching {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        firestore
            .collection(FirestoreCollections.ORDERS)
            .whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .get()
            .await()
            .size()
    }

    suspend fun getOrdersLast10Days(): Result<List<Pair<String, Int>>> = runCatching {
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Build ordered labels D-9 → today
        val dayLabels = (9 downTo 0).map { daysBack ->
            Calendar.getInstance().apply {
                timeInMillis = todayStart.timeInMillis
                add(Calendar.DAY_OF_YEAR, -daysBack)
            }.let { dateFormat.format(it.time) }
        }

        val startTimestamp = Calendar.getInstance().apply {
            timeInMillis = todayStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, -9)
        }.timeInMillis

        val documents = firestore
            .collection(FirestoreCollections.ORDERS)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .get()
            .await()
            .documents

        val countMap = dayLabels.associateWith { 0 }.toMutableMap()
        documents.forEach { doc ->
            val createdAt = (doc.data?.get("createdAt") as? Number)?.toLong() ?: return@forEach
            val label = dateFormat.format(Date(createdAt))
            countMap[label] = (countMap[label] ?: 0) + 1
        }

        dayLabels.map { it to countMap.getValue(it) }
    }
}
