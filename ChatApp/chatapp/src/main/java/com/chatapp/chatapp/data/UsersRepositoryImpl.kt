package com.chatapp.chatapp.data

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.chatapp.chatapp.domain.UsersRepository
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.util.Resource
import com.chatapp.chatapp.util.UpdateStatusWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : UsersRepository {


    override fun saveUserToDatabase(user: Map<String, Any?>) {
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        firebaseFirestore.collection("users").document(currentUserId).set(user)
            .addOnSuccessListener {
                firebaseAuth.signOut()
            }
    }

    override suspend fun getUsersList(): Flow<Resource<List<User>>> {
        return flow {
            emit(Resource.Loading())
            try {
                val result = firebaseFirestore.collection("users").get(Source.DEFAULT).await()
                val usersList = result.map { document ->
                    User(
                        userId = document.getString("userId") ?: "",
                        avatar = document.getString("avatar"),
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        password = document.getString("password") ?: "",
                        online = document.getBoolean("online") ?: false,
                        lastSeen = document.getTimestamp("lastSeen")?.toDate() ?: Date(0)
                    )
                }
                emit(Resource.Success(usersList))
                Log.d("UserList",usersList.toString())
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

    override suspend fun searchUsers(query: String): Flow<List<User>> = flow {
        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        try {
            val result = firebaseFirestore
                .collection("users")
                .get(Source.DEFAULT)
                .await()

            val usersList = result.documents.map { document ->
                User(
                    userId = document.getString("userId") ?: "",
                    avatar = document.getString("avatar"),
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    password = document.getString("password") ?: "",
                    online = document.getBoolean("online") ?: false,
                    lastSeen = document.getTimestamp("lastSeen")?.toDate() ?: Date(0),
                    friends = document.get("friends") as? List<String> ?: emptyList()
                )
            }
             val listFiltered = usersList.filter {
                 it.userId != currentUserId && it.name.contains(query, ignoreCase = true)
            }
            emit(listFiltered)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }



    override fun updateUserStatus(userId: String, isOnline: Boolean,onSuccesUpdateStatus:() -> Unit) {
        val userStatusUpdate = mapOf(
            "online" to isOnline,
            "lastSeen" to FieldValue.serverTimestamp()
        )

        firebaseFirestore.collection("users").document(userId)
            .update(userStatusUpdate)
            .addOnSuccessListener {
                onSuccesUpdateStatus()
                Log.d("MainActivity", "User status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to update user status.", e)
            }
    }
    override fun scheduleUpdateUserStatusWork(userId: String, isOnline: Boolean) {
        // Создаем данные для передачи в Worker
        val inputData = workDataOf(
            "userId" to userId,
            "isOnline" to isOnline
        )

        // Создаем OneTimeWorkRequest для запуска задачи один раз
        val workRequest = OneTimeWorkRequestBuilder<UpdateStatusWorker>()
            .setInputData(inputData)
            .build()

        // Запускаем WorkManager для выполнения задачи
        WorkManager.getInstance(context).enqueue(workRequest)
    }


    override fun listenForUserStatusChanges(userId: String, onStatusChanged: (Pair<Boolean,Date>) -> Unit) {
        val userRef = firebaseFirestore.collection("users").document(userId)

        userRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val status = snapshot.getBoolean("online") ?: false
            val lastSeen = snapshot.getTimestamp("lastSeen")?.toDate() ?: Date(0)
            onStatusChanged(Pair(status,lastSeen))
        }
    }
}