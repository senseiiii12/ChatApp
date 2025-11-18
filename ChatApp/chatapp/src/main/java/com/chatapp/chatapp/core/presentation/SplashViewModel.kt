package com.chatapp.chatapp.core.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.navigation.AuthFlow
import com.chatapp.chatapp.features.navigation.MainFlow
import com.chatapp.chatapp.features.navigation.Route
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    val isLoading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            // Минимальная задержка для splash screen
            delay(1000)
            isLoading.value = false
        }
    }

    /**
     * Старый метод для обратной совместимости
     */
    fun checkUser(): String {
        return if (auth.currentUser != null) {
            Route.HomePage.route
        } else {
            Route.MainEntrance.route
        }
    }

    /**
     * Новый типобезопасный метод
     */
    fun checkUserTypeSafe(): Any {
        return if (auth.currentUser != null) {
            MainFlow
        } else {
            AuthFlow
        }
    }
}