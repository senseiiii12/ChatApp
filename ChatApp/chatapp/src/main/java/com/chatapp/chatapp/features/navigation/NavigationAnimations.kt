package com.chatapp.chatapp.features.navigation


import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

/**
 * Типы анимаций для навигации
 */
sealed class NavigationAnimation {
    // Горизонтальное скольжение (по умолчанию)
    object SlideHorizontal : NavigationAnimation()

    // Вертикальное скольжение (для модальных окон)
    object SlideVertical : NavigationAnimation()

    // Fade анимация
    object Fade : NavigationAnimation()

    // Масштабирование
    object Scale : NavigationAnimation()

    // Без анимации
    object None : NavigationAnimation()

    // Комбинированная (slide + fade)
    object SlideAndFade : NavigationAnimation()
}

/**
 * Конфигурация для spring анимации
 */
object SpringConfig {
    val default = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val smooth = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val bouncy = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

/**
 * Горизонтальное скольжение (слева направо)
 */
fun slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = spring()
    )
}

fun slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = spring()
    )
}

fun slideInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = spring()
    )
}

fun slideOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = spring()
    )
}

/**
 * Вертикальное скольжение (снизу вверх)
 */
fun slideInFromBottom(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = SpringConfig.default
    )
}

fun slideOutToBottom(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = SpringConfig.default
    )
}

fun slideInFromTop(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> -fullHeight },
        animationSpec = SpringConfig.default
    )
}

fun slideOutToTop(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> -fullHeight },
        animationSpec = SpringConfig.default
    )
}

/**
 * Fade анимации
 */
fun fadeInAnimation(durationMillis: Int = 300): EnterTransition {
    return fadeIn(animationSpec = tween(durationMillis))
}

fun fadeOutAnimation(durationMillis: Int = 300): ExitTransition {
    return fadeOut(animationSpec = tween(durationMillis))
}

/**
 * Scale анимации
 */
fun scaleInAnimation(
    initialScale: Float = 0.9f,
    durationMillis: Int = 300
): EnterTransition {
    return scaleIn(
        initialScale = initialScale,
        animationSpec = tween(durationMillis)
    ) + fadeIn(animationSpec = tween(durationMillis))
}

fun scaleOutAnimation(
    targetScale: Float = 0.9f,
    durationMillis: Int = 300
): ExitTransition {
    return scaleOut(
        targetScale = targetScale,
        animationSpec = tween(durationMillis)
    ) + fadeOut(animationSpec = tween(durationMillis))
}

/**
 * Data class для хранения набора анимаций
 */
data class TransitionSet(
    val enter: EnterTransition,
    val exit: ExitTransition,
    val popEnter: EnterTransition,
    val popExit: ExitTransition
)

/**
 * Предустановленные наборы анимаций
 */
object NavigationTransitions {
    // Стандартная горизонтальная навигация
    val slideHorizontal = TransitionSet(
        enter = slideInFromRight() + fadeIn(),
        exit = slideOutToLeft() + fadeOut(),
        popEnter = slideInFromLeft() + fadeIn(),
        popExit = slideOutToRight() + fadeOut()
    )

    // Модальная вертикальная навигация
    val slideVertical = TransitionSet(
        enter = slideInFromBottom() + fadeIn(),
        exit = fadeOut(),
        popEnter = fadeIn(),
        popExit = slideOutToBottom() + fadeOut()
    )

    // Только fade
    val fade = TransitionSet(
        enter = fadeInAnimation(),
        exit = fadeOutAnimation(),
        popEnter = fadeInAnimation(),
        popExit = fadeOutAnimation()
    )

    // Scale + fade
    val scale = TransitionSet(
        enter = scaleInAnimation(),
        exit = scaleOutAnimation(),
        popEnter = scaleInAnimation(),
        popExit = scaleOutAnimation()
    )

    // Без анимации
    val none = TransitionSet(
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        popEnter = EnterTransition.None,
        popExit = ExitTransition.None
    )

    // Плавная горизонтальная навигация (smooth spring)
    val slideHorizontalSmooth = TransitionSet(
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = SpringConfig.smooth
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = SpringConfig.smooth
        ) + fadeOut(),
        popEnter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = SpringConfig.smooth
        ) + fadeIn(),
        popExit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = SpringConfig.smooth
        ) + fadeOut()
    )

    // Bouncy анимация для игривого эффекта
    val slideHorizontalBouncy = TransitionSet(
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = SpringConfig.bouncy
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = SpringConfig.bouncy
        ) + fadeOut(),
        popEnter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = SpringConfig.bouncy
        ) + fadeIn(),
        popExit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = SpringConfig.bouncy
        ) + fadeOut()
    )
}

/**
 * Получить набор анимаций по типу
 */
fun getTransitions(animation: NavigationAnimation): TransitionSet {
    return when (animation) {
        NavigationAnimation.SlideHorizontal -> NavigationTransitions.slideHorizontal
        NavigationAnimation.SlideVertical -> NavigationTransitions.slideVertical
        NavigationAnimation.Fade -> NavigationTransitions.fade
        NavigationAnimation.Scale -> NavigationTransitions.scale
        NavigationAnimation.None -> NavigationTransitions.none
        NavigationAnimation.SlideAndFade -> NavigationTransitions.slideHorizontalSmooth
    }
}