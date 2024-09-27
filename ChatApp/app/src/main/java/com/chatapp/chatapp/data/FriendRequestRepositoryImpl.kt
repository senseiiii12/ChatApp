package com.chatapp.chatapp.data

import android.util.Log
import com.chatapp.chatapp.domain.FriendRequestRepository
import com.chatapp.chatapp.domain.models.FriendRequest
import com.chatapp.chatapp.domain.models.FriendRequestWithUser
import com.chatapp.chatapp.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class FriendRequestRepositoryImpl : FriendRequestRepository {

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()
    override suspend fun sendFriendRequest(toUserId: String, onResult: (Boolean) -> Unit) {
        try {
            val existingRequestSnapshot = firestore.collection("friend_requests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()

            if (!existingRequestSnapshot.isEmpty) {
                onResult(false)
                return
            }
            val requestRef = firestore.collection("friend_requests").document()
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

    override suspend fun getPendingFriendRequestsWithUserInfo(): List<FriendRequestWithUser> {
        return try {
            val snapshot = firestore
                .collection("friend_requests")
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val friendRequests = snapshot.toObjects(FriendRequest::class.java)

            val friendRequestsWithUsers = friendRequests.mapNotNull { request ->
                val userSnapshot = firestore.collection("users")
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

    override suspend fun respondToFriendRequest(requestId: String, accept: Boolean, onResult: (Boolean) -> Unit) {
        try {
            val requestRef = firestore.collection("friend_requests").document(requestId)
            val requestSnapshot = requestRef.get().await()
            val friendRequest = requestSnapshot.toObject(FriendRequest::class.java)

            if (friendRequest == null) {
                Log.e("FriendRequest", "Request not found with ID: $requestId")
                return
            }

            if (accept) {
                addFriendsToEachUser(friendRequest.fromUserId, friendRequest.toUserId){
                    requestRef.delete().addOnSuccessListener { onResult(true) }
                }
            } else {
                requestRef.delete().addOnSuccessListener { onResult(true) }
            }
        } catch (e: Exception) {
            Log.e("FriendRequest", "Error responding to friend request: ${e.message}")
        }
    }

    private suspend fun addFriendsToEachUser(userId1: String, userId2: String, onSuccess: () -> Unit) {
        try {
            firestore.collection("users").document(userId1)
                .update("friends", FieldValue.arrayUnion(userId2)).await()

            firestore.collection("users").document(userId2)
                .update("friends", FieldValue.arrayUnion(userId1)).await()

            onSuccess()

        } catch (e: Exception) {
            Log.e("Friends", "Error adding friends: ${e.message}")
        }
    }
}