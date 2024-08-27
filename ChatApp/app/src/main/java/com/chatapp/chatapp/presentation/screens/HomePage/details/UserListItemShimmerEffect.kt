package com.chatapp.chatapp.presentation.screens.HomePage.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chatapp.chatapp.presentation.screens.HomePage.UserListState
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.Outline_Card
import com.chatapp.chatapp.ui.theme.Surface_Card

@Composable
fun UserListItemShimmerEffect(
    state: UserListState
) {

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = state.isLoading) {
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Outline_Card, Outline_Card.copy(alpha = 0.3f))),
                    RoundedCornerShape(22.dp)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(Surface_Card)
                .height(90.dp)
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(70.dp)
                    .shimmerEffect(),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .height(18.dp)
                            .widthIn(80.dp)
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .height(18.dp)
                            .widthIn(30.dp)
                            .shimmerEffect()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 30.dp)
                            .clip(CircleShape)
                            .height(18.dp)
                            .weight(1f)
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(20.dp)
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserListItemShimmerEffectPreview() {
    ChatAppTheme {
        UserListItemShimmerEffect(
            state = UserListState(
                isLoading = true,
                isSuccess = emptyList()
            )
        )
    }
}