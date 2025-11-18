package com.chatapp.chatapp.features.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * Composable с предустановленными анимациями
 */
inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    animation: NavigationAnimation = NavigationAnimation.SlideHorizontal,
    noinline content: @Composable (NavBackStackEntry) -> Unit
) {
    val transitions = getTransitions(animation)

    composable<T>(
        enterTransition = { transitions.enter },
        exitTransition = { transitions.exit },
        popEnterTransition = { transitions.popEnter },
        popExitTransition = { transitions.popExit }
    ) { backStackEntry ->
        content(backStackEntry)
    }
}

/**
 * Navigation graph с предустановленными анимациями
 */
inline fun <reified T : Any, reified START_DESTINATION : Any> NavGraphBuilder.animatedNavigation(
    animation: NavigationAnimation = NavigationAnimation.SlideHorizontal,
    noinline builder: NavGraphBuilder.() -> Unit
) {
    val transitions = getTransitions(animation)

    navigation<T>(
        startDestination = START_DESTINATION::class,
        enterTransition = { transitions.enter },
        exitTransition = { transitions.exit },
        popEnterTransition = { transitions.popEnter },
        popExitTransition = { transitions.popExit },
        builder = builder
    )
}