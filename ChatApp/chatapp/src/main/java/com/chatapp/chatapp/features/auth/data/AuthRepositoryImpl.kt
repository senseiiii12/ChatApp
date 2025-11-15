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

    override fun getCurrentUserUID(): String? {
        return firebaseAuth.currentUser?.uid
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

                    // ===== ОШИБКИ EMAIL =====
                    "ERROR_INVALID_EMAIL" -> "Некорректный формат email"
                    "ERROR_MISSING_EMAIL" -> "Укажите email"
                    "ERROR_INVALID_RECIPIENT_EMAIL" -> "Некорректный email получателя"

                    // ===== ОШИБКИ ВХОДА =====
                    "ERROR_WRONG_PASSWORD" -> "Неверный пароль"
                    "ERROR_USER_NOT_FOUND" -> "Пользователь с таким email не найден"
                    "ERROR_USER_DISABLED" -> "Этот аккаунт был отключен администратором"

                    // ===== ОШИБКИ РЕГИСТРАЦИИ =====
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Пользователь с таким email уже существует"
                    "ERROR_WEAK_PASSWORD" -> "Пароль слишком слабый. Используйте минимум 6 символов"

                    // ===== СЕТЕВЫЕ ОШИБКИ =====
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Проблемы с подключением к интернету. Проверьте соединение"

                    // ===== ОГРАНИЧЕНИЯ =====
                    "ERROR_TOO_MANY_REQUESTS" -> "Слишком много попыток. Попробуйте позже"
                    "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Слишком много попыток. Попробуйте позже"
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Этот метод входа не активирован в настройках Firebase"

                    // ===== ОШИБКИ ТОКЕНОВ/СЕССИЙ =====
                    "ERROR_INVALID_USER_TOKEN" -> "Сессия истекла. Войдите заново"
                    "ERROR_USER_TOKEN_EXPIRED" -> "Сессия истекла. Войдите заново"
                    "ERROR_REQUIRES_RECENT_LOGIN" -> "Необходимо войти заново для выполнения этого действия"
                    "ERROR_INVALID_CUSTOM_TOKEN" -> "Неверный токен аутентификации"
                    "ERROR_CUSTOM_TOKEN_MISMATCH" -> "Токен не соответствует проекту Firebase"

                    // ===== КОНФЛИКТЫ АККАУНТОВ =====
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                        "Аккаунт с этим email уже существует. Используйте другой метод входа"
                    "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                        "Эти учетные данные уже используются другим аккаунтом"

                    // ===== ДОПОЛНИТЕЛЬНЫЕ ОШИБКИ =====
                    "ERROR_MISSING_PASSWORD" -> "Укажите пароль"
                    "ERROR_MISSING_PHONE_NUMBER" -> "Укажите номер телефона"
                    "ERROR_INVALID_PHONE_NUMBER" -> "Некорректный номер телефона"
                    "ERROR_MISSING_VERIFICATION_CODE" -> "Укажите код подтверждения"
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Неверный код подтверждения"
                    "ERROR_SESSION_EXPIRED" -> "Сессия истекла. Попробуйте снова"

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

        // Ищем паттерн [ ERROR_CODE ]
        val regex = "\\[\\s*([A-Z_]+)\\s*\\]".toRegex()
        val matchResult = regex.find(message)

        return matchResult?.groupValues?.getOrNull(1)?.also {
            Log.d(TAG, "Extracted error code from message: '$it'")
        }
    }
}