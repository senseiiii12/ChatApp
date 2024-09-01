package com.chatapp.chatapp.data

import android.util.Log
import com.chatapp.chatapp.domain.FirebaseDatabaseRepository
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class FirebaseDatabaseRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : FirebaseDatabaseRepository {


    override fun saveUserToDatabase(user: Map<String, Any?>) {
        val userUid = firebaseAuth.currentUser?.uid ?: ""
        firebaseFirestore.collection("users").document(userUid).set(user)
            .addOnSuccessListener {
                firebaseAuth.signOut()
            }
            .addOnFailureListener {
                // Обработка ошибки
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

    override fun updateUserStatus(userId: String, isOnline: Boolean) {
        val userStatusUpdate = mapOf(
            "online" to isOnline,
            "lastSeen" to FieldValue.serverTimestamp()
        )

        firebaseFirestore.collection("users").document(userId)
            .update(userStatusUpdate)
            .addOnSuccessListener {
                Log.d("MainActivity", "User status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to update user status.", e)
            }
    }

    override fun listenForUserStatusChanges(userId: String, onStatusChanged: (Boolean) -> Unit) {
        val userRef = firebaseFirestore.collection("users").document(userId)

        userRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val status = snapshot.getBoolean("online") ?: false
            onStatusChanged(status)
        }
    }
}