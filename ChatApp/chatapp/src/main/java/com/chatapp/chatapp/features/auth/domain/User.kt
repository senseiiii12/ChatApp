package com.chatapp.chatapp.features.auth.domain

import androidx.compose.runtime.Immutable
import com.chatapp.chatapp.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Immutable
@Serializable
data class User(
    val userId: String = "",
    val avatar: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val online: Boolean = false,
    @Serializable(with = DateSerializer::class)
    val lastSeen: Date = Date(),
    val friends: List<String> = emptyList()
)