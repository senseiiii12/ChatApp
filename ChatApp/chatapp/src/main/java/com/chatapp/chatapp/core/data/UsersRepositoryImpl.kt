package com.chatapp.chatapp.core.data

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.util.Resource
import com.chatapp.chatapp.util.UpdateStatusWorker
import com.chatapp.chatapp.util.extension.toUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : UsersRepository {

    private val currentUserId: String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    override suspend fun getCurrentUser(): Flow<User> {
        return flow {
            val result = currentUserId?.let {
                firebaseFirestore
                    .collection("users")
                    .document(it)
                    .get().await()
            }
            result?.toUser()?.let { emit(it) }
        }
    }

    override suspend fun getUsersList(): Flow<Resource<List<User>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val result = firebaseFirestore.collection("users").get(Source.DEFAULT).await()
                val usersList = result.documents.map { document ->
                    document.toUser()
                }
                emit(Resource.Success(usersList))
                Log.d("UserList", usersList.toString())
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

   override suspend fun searchUsers(query: String): Flow<List<User>> = flow {
       Log.d("searchUsers", currentUserId.toString())
        try {
            val snapshot = firebaseFirestore.collection("users")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get(Source.DEFAULT)
                .await()

            val usersList = snapshot.documents
                .map { it.toUser() }
                .filter { it.userId != currentUserId }

            emit(usersList)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        val inputData = workDataOf(
            "userId" to userId,
            "isOnline" to isOnline
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UpdateStatusWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "update_user_status",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }


    override fun listenForUserStatusChanges(
        userId: String,
        onStatusChanged: (Pair<Boolean, Date>) -> Unit
    ) {
        val userRef = firebaseFirestore.collection("users").document(userId)

        userRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val status = snapshot.getBoolean("online") ?: false
            val lastSeen = snapshot.getTimestamp("lastSeen")?.toDate() ?: Date(0)
            onStatusChanged(Pair(status, lastSeen))
        }
    }
}