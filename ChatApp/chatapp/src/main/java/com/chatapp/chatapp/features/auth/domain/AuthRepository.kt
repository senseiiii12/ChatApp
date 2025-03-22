package com.chatapp.chatapp.features.auth.domain

import com.chatapp.chatapp.util.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getCurrentUser(): FirebaseUser?
    fun getCurrentUserUID(): String?
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun registerUser(email: String, password: String): Flow<Resource<AuthResult>>
    fun saveUserToDatabase(user: Map<String, Any?>)
    fun signOut()
    suspend fun forgotPassword(email: String)

}