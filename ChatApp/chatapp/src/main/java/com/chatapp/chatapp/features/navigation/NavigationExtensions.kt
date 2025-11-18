package com.chatapp.chatapp.features.navigation

import androidx.navigation.NavController

/**
 * Навигация на главный поток (после успешной авторизации)
 */
fun NavController.navigateToMainFlow() {
    navigate(MainFlow) {
        popUpTo(AuthFlow) { inclusive = true }
    }
}

/**
 * Навигация на поток авторизации (при выходе)
 */
fun NavController.navigateToAuthFlow() {
    navigate(AuthFlow) {
        popUpTo(0) { inclusive = true }
    }
}