package com.seemoo.openflow.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * FreemiumManager - Modified to always allow unlimited access (app is now fully free)
 * All methods return unlimited/pro status to maintain compatibility with existing code
 */
class FreemiumManager(private val context: Context? = null) {

    private val db = Firebase.firestore

    private fun getUserId(): String? {
        return context?.let {
            UserIdManager(it).getOrCreateUserId()
        }
    }

    companion object {
        const val DAILY_TASK_LIMIT = Long.MAX_VALUE // Unlimited tasks
        private const val PRO_SKU = "pro" // Kept for compatibility
    }

    suspend fun getDeveloperMessage(): String {
        // Return empty string as no developer message needed for free app
        return ""
    }

    /**
     * Always returns true - app is fully free
     */
    suspend fun isUserSubscribed(): Boolean {
        return true // All users have unlimited access
    }

    suspend fun provisionUserIfNeeded() {
        val userId = getUserId() ?: return
        val userDocRef = db.collection("users").document(userId)

        try {
            val document = userDocRef.get().await()
            if (!document.exists()) {
                Logger.d("FreemiumManager", "Provisioning new user: $userId")
                val newUser = hashMapOf(
                    "userId" to userId,
                    "plan" to "pro", // Set everyone to pro by default
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(newUser).await()
            }
        } catch (e: Exception) {
            Logger.e("FreemiumManager", "Error provisioning user", e)
        }
    }

    /**
     * Always returns unlimited tasks
     */
    suspend fun getTasksRemaining(): Long {
        return Long.MAX_VALUE // Unlimited tasks
    }

    /**
     * Always returns true - unlimited access
     */
    suspend fun canPerformTask(): Boolean {
        return true // Always allow tasks
    }

    /**
     * Does nothing - no need to decrement when unlimited
     */
    suspend fun decrementTaskCount() {
        // No need to decrement - unlimited tasks
        Logger.d("FreemiumManager", "Task executed (unlimited mode - no decrement needed)")
    }
}