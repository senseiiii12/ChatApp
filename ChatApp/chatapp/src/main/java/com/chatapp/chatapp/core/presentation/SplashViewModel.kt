package com.chatapp.chatapp.core.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.navigation.RouteTypeSafe
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
            delay(1000)
            isLoading.value = false
        }
    }

    fun checkUserTypeSafe(): RouteTypeSafe.Graph {
        return if (auth.currentUser != null) {
            RouteTypeSafe.Graph.ChatRoomsNavGraph
        } else {
            RouteTypeSafe.Graph.AuthNavGraph
        }
    }
}