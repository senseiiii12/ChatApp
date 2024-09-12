package com.chatapp.chatapp.domain.models

import kotlinx.serialization.Serializable
import java.util.Date


data class User(
    val userId: String,
    val avatar: String? = null,
    val name: String,
    val email: String,
    val password: String,
    val online: Boolean = false,
    val lastSeen: Date
)
