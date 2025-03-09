package com.chatapp.chatapp.domain.models

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import java.util.Date

@Stable
data class User(
    val userId: String = "",
    val avatar: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val online: Boolean = false,
    val lastSeen: Date = Date(),
    val friends: List<String> = emptyList()
)
