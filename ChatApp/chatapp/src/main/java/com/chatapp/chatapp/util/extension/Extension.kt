package com.chatapp.chatapp.util.extension

import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

fun DocumentSnapshot.toUser(): User {
    return User(
        userId = getString("userId") ?: "",
        avatar = getString("avatar"),
        name = getString("name") ?: "",
        email = getString("email") ?: "",
        password = getString("password") ?: "",
        online = getBoolean("online") ?: false,
        lastSeen = getTimestamp("lastSeen")?.toDate() ?: Date(0),
        friends = get("friends") as? List<String> ?: emptyList()
    )
}

fun DocumentSnapshot.toMessage(): Message {
    return Message(
        userId = getString("userId") ?: "",
        text = getString("text") ?: "",
        timestamp = getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis(),
        messageId = getString("messageId") ?: "",
        status = MessageStatus.valueOf(getString("status") ?: MessageStatus.SENT.name)
    )
}