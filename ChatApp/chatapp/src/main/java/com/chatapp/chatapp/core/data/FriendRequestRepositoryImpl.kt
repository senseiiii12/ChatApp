package com.chatapp.chatapp.core.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.core.domain.FriendRequestRepository
import com.chatapp.chatapp.core.domain.models.FriendRequest
import com.chatapp.chatapp.core.domain.models.FriendRequestWithUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FriendRequestRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : FriendRequestRepository {

    private val currentUserId: String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    override suspend fun sendFriendRequest(toUserId: String, onResult: (Boolean) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        try {
            val existingRequestSnapshot = firebaseFirestore.collection("friend_requests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()

            if (!existingRequestSnapshot.isEmpty) {
                onResult(false)
                return
            }
            val requestRef = firebaseFirestore.collection("friend_requests").document()
            val request = FriendRequest(
                id = requestRef.id,
                fromUserId = currentUserId ?: "",
                toUserId = toUserId,
                status = "pending"
            )
            requestRef.set(request).addOnSuccessListener {
                onResult(true)
            }
        } catch (e: Exception) {
            Log.e("FriendRequest", "Error sending friend request: ${e.message}")
        }
    }

    override suspend fun getFriendRequestsForSearchUser(): Flow<List<FriendRequest>> = flow {
        try {
            val incomingSnapshot = firebaseFirestore
                .collection("friend_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get(Source.DEFAULT)
                .await()

            val outgoingSnapshot = firebaseFirestore
                .collection("friend_requests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get(Source.DEFAULT)
                .await()

            val requests = (incomingSnapshot.documents + outgoingSnapshot.documents)
                .mapNotNull { it.toObject(FriendRequest::class.java) }

            emit(requests)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getPendingFriendRequestsWithUserInfo(): List<FriendRequestWithUser> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("currentUserId-getPendingFriendRequestsWithUserInfo", currentUserId.toString())
        return try {
            val snapshot = firebaseFirestore
                .collection("friend_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val friendRequests = snapshot.toObjects(FriendRequest::class.java)

            val friendRequestsWithUsers = friendRequests.mapNotNull { request ->
                val userSnapshot = firebaseFirestore.collection("users")
                    .document(request.fromUserId)
                    .get()
                    .await()

                val user = userSnapshot.toObject(User::class.java)
                Log.d("Friend_user", user.toString())
                if (user != null) {
                    FriendRequestWithUser(friendRequest = request, user = user)
                } else {
                    null
                }
            }
            friendRequestsWithUsers
        } catch (e: Exception) {
            println("Error fetching friend requests or user info: ${e.message}")
            emptyList()
        }
    }

    override suspend fun respondToFriendRequest(
        friendRequest: FriendRequest,
        accept: Boolean
    ): RespondFriendResult {
        return try {
            val requestRef = firebaseFirestore.collection("friend_requests").document(friendRequest.id)

            if (accept) {
                acceptFriendRequest(
                    friendRequest.fromUserId,
                    friendRequest.toUserId,
                    friendRequest.id
                )
                RespondFriendResult.SuccessAccept
            } else {
                requestRef.delete().await()
                RespondFriendResult.SuccessDecline
            }
        } catch (e: Exception) {
            Log.e("FriendRequest", "Error responding to friend request: ${e.message}", e)
            if (accept) RespondFriendResult.ErrorAccept else RespondFriendResult.ErrorDecline
        }
    }

    private suspend fun acceptFriendRequest(
        userId1: String,
        userId2: String,
        friendRequestId: String
    ) {
        val user1Ref = firebaseFirestore.collection("users").document(userId1)
        val user2Ref = firebaseFirestore.collection("users").document(userId2)
        val requestRef = firebaseFirestore.collection("friend_requests").document(friendRequestId)

        firebaseFirestore.runTransaction { transaction ->
            transaction.update(user1Ref, "friends", FieldValue.arrayUnion(userId2))
            transaction.update(user2Ref, "friends", FieldValue.arrayUnion(userId1))
            transaction.delete(requestRef)
        }.await()
    }

    sealed class RespondFriendResult {
        object SuccessAccept : RespondFriendResult()
        object ErrorAccept : RespondFriendResult()
        object SuccessDecline : RespondFriendResult()
        object ErrorDecline : RespondFriendResult()
    }

}