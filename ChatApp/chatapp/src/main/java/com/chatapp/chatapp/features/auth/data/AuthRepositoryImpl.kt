package com.chatapp.chatapp.features.auth.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.util.Resource
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

    override fun loginUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch { exception ->
            // Детальное логирование для отладки
            Log.e(TAG, "=== LOGIN ERROR ===")
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

    override fun registerUser(email: String, password: String): Flow<Resource<AuthResult>> {
        return flow {
            emit(Resource.Loading())
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result))
        }.catch { exception ->
            emit(Resource.Error(handleAuthException(exception)))
        }
    }

    override fun saveUserToDatabase(user: Map<String, Any?>) {
        val currentUserId = getCurrentUserUID() ?: ""
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
        // Извлекаем код ошибки из сообщения если это FirebaseException
        val errorCode = when (exception) {
            is FirebaseAuthException -> exception.errorCode
            else -> extractErrorCodeFromMessage(exception.message)
        }

        return when (exception) {
            is FirebaseAuthException -> {
                val errorMessage = when (errorCode) {

                    "INVALID_LOGIN_CREDENTIALS" -> "Неверный email или пароль"
                    "ERROR_INVALID_CREDENTIAL" -> "Неверные учетные данные"

                    // ===== ОШИБКИ ВХОДА =====
                    "ERROR_WRONG_PASSWORD" -> "Неверный пароль"
                    "ERROR_USER_NOT_FOUND" -> "Пользователь с таким email не найден"
                    "ERROR_USER_DISABLED" -> "Этот аккаунт был отключен администратором"

                    // ===== ОШИБКИ РЕГИСТРАЦИИ =====
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Пользователь с таким email уже существует"
                    "ERROR_WEAK_PASSWORD" -> "Пароль слишком слабый. Используйте минимум 6 символов"

                    else -> {
                        "Ошибка авторизации: ${exception.message ?: "Неизвестная ошибка"}"
                    }
                }
                errorMessage
            }
            else -> {
                val errorMessage = extractErrorCodeFromMessage(exception.message)

                if (errorMessage != null) {
                    Log.d(TAG, "Extracted error code from message: $errorMessage")
                    when (errorMessage) {
                        "INVALID_LOGIN_CREDENTIALS" -> "Неверный email или пароль"
                        "EMAIL_NOT_FOUND" -> "Пользователь с таким email не найден"
                        "INVALID_PASSWORD" -> "Неверный пароль"
                        "EMAIL_EXISTS" -> "Пользователь с таким email уже существует"
                        "WEAK_PASSWORD" -> "Пароль слишком слабый. Используйте минимум 6 символов"
                        "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Слишком много попыток. Попробуйте позже"
                        else -> "Ошибка авторизации: $errorMessage"
                    }
                } else {
                    Log.e(TAG, "Non-Firebase exception in handleAuthException", exception)
                    exception.message ?: "Произошла неизвестная ошибка"
                }
            }
        }
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