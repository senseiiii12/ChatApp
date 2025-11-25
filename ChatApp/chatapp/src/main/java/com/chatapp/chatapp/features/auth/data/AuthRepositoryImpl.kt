package com.chatapp.chatapp.features.auth.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.util.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun getCurrentUserUID(): String {
        return firebaseAuth.currentUser?.uid ?: ""
    }

    override fun signInUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch { exception ->
            Log.e(TAG, "=== LOGIN ERROR ===")
            Log.e(TAG, "Exception class: ${exception.javaClass.simpleName}")

            if (exception is FirebaseAuthException) {
                Log.e(TAG, "⚠️ ERROR CODE: ${exception.errorCode}")
                Log.e(TAG, "Error message: ${exception.message}")
            }
            else if (exception is FirebaseException){
                Log.e(TAG, "⚠️ ERROR CODE: ${exception.cause}")
                Log.e(TAG, "Error message: ${exception.message}")
            }
            else {
                Log.e(TAG, "Non-Firebase exception: ${exception.message}")
            }
            Log.e(TAG, "==================")

            emit(Resource.Error(handleAuthException(exception)))
        }
    }

    override fun signUpUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch { exception ->
            Log.e(TAG, "=== REgister ERROR ===")
            Log.e(TAG, "Exception class: ${exception.javaClass.simpleName}")

            if (exception is FirebaseAuthException) {
                Log.e(TAG, "⚠️ ERROR CODE: ${exception.errorCode}")
                Log.e(TAG, "Error message: ${exception.message}")
            } else {
                Log.e(TAG, "Non-Firebase exception: ${exception.message}")
            }
            Log.e(TAG, "==================")
            emit(Resource.Error(handleAuthException(exception)))
        }
    }

    override fun saveUserToDatabase(user: Map<String, Any?>) {
        val currentUserId = getCurrentUserUID()
        firebaseFirestore.collection("users").document(currentUserId).set(user)
            .addOnSuccessListener {
                firebaseAuth.signOut()
            }
            .addOnFailureListener { exception ->
                Log.e("AuthRepository", "Error saving user: ${exception.message}")
            }
    }

    override suspend fun updateUserAvatar(userId: String, avatarUrl: String) {
        firebaseFirestore.collection("users")
            .document(userId)
            .update("avatar", avatarUrl)
            .await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun forgotPassword(email: String) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
        } catch (e: FirebaseAuthException) {
            throw Exception(handleAuthException(e))
        } catch (e: Exception) {
            throw Exception("Не удалось отправить письмо для сброса пароля")
        }
    }


    private fun handleAuthException(exception: Throwable): String {
        val errorCode = when (exception) {
            is FirebaseAuthException -> exception.errorCode
            else -> extractErrorCodeFromMessage(exception.message)
        }

        return when (errorCode) {
            "INVALID_LOGIN_CREDENTIALS" -> "Неверный email или пароль"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Пользователь уже существует"
            else -> {}
        }.toString()
    }

    /**
     * Извлекает код ошибки из сообщения вида "An internal error has occurred. [ ERROR_CODE ]"
     */
    private fun extractErrorCodeFromMessage(message: String?): String? {
        if (message == null) return null
        val regex = "\\[\\s*([A-Z_]+)\\s*\\]".toRegex()
        val matchResult = regex.find(message)
        return matchResult?.groupValues?.getOrNull(1)?.also {
            Log.d(TAG, "Extracted error code from message: '$it'")
        }
    }
}