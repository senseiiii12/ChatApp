package com.chatapp.chatapp.util

object ChatIdGenerator {

    fun generateChatId(currentUserId: String, otherUserId: String): String {
        return if (currentUserId < otherUserId) {
            "${currentUserId}-${otherUserId}"
        } else {
            "${otherUserId}-${currentUserId}"
        }
    }

}