package com.chatapp.chatapp.util

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UpdateStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val userId = inputData.getString("userId") ?: return Result.failure()
        val isOnline = inputData.getBoolean("isOnline", false)

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val userStatusUpdate = mapOf(
            "online" to isOnline,
            "lastSeen" to FieldValue.serverTimestamp()
        )

        return try {
            firebaseFirestore.collection("users").document(userId)
                .update(userStatusUpdate)
                .addOnSuccessListener {
                    Log.d("UpdateStatusWorker", "User status updated successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateStatusWorker", "Failed to update user status.", e)
                }

            Result.success()
        } catch (e: Exception) {
            Log.e("UpdateStatusWorker", "Error updating user status.", e)
            Result.retry()
        }
    }
}