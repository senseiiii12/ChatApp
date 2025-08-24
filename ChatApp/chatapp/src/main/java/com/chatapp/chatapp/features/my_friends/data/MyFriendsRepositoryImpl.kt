package com.chatapp.chatapp.features.my_friends.data

import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.my_friends.domain.MyFriendsRepository
import com.chatapp.chatapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MyFriendsRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : MyFriendsRepository {

    private val currentUserId: String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    override suspend fun getMyFriends(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())

        try {
            val userDoc = firebaseFirestore.collection("users").document(currentUserId).get().await()

            val friendsIds = userDoc.get("friends") as? List<String> ?: emptyList()

            if (friendsIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val friends = mutableListOf<User>()
            val chunks = friendsIds.chunked(10)

            for (chunk in chunks) {
                val snapshot = firebaseFirestore.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                friends += snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
            }

            emit(Resource.Success(friends))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ошибка при получении друзей"))
        }
    }

    override suspend fun deleteFriend() {
        TODO("Not yet implemented")
    }
}