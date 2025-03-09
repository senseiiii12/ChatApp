package com.chatapp.chatapp.presentation.screens.HomePage.details

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.shimmerEffect() = composed {
    val transition = rememberInfiniteTransition()
    val shimmerX = transition.animateFloat(
        initialValue = -300f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing)
        ),
        label = ""
    ).value

    val brush = Brush.linearGradient(
        colors = listOf(
            DarkGray_2,
            DarkGray_1.copy(alpha = 0.4f),
            DarkGray_2
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 300f, 0f)
    )

    background(brush = brush)
}