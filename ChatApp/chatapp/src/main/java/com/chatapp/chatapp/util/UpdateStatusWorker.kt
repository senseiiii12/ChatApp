package com.chatapp.chatapp.util

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
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
            val task = firebaseFirestore.collection("users").document(userId)
                .update(userStatusUpdate)

            Tasks.await(task)

            if (task.isSuccessful) {
                Log.d("UpdateStatusWorker", "User status updated successfully.")
                Result.success()
            } else {
                Log.e("UpdateStatusWorker", "Failed to update user status.", task.exception)
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("UpdateStatusWorker", "Error updating user status.", e)
            Result.retry()
        }
    }
}