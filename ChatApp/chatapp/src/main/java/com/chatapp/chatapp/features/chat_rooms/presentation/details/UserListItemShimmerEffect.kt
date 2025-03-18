package com.chatapp.chatapp.features.chat_rooms.presentation.details

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.Outline_Card
import com.chatapp.chatapp.ui.theme.Surface_Card

@Composable
fun UserListItemShimmerEffect() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                1.dp,
                Brush.linearGradient(listOf(Outline_Card, Outline_Card.copy(alpha = 0.3f))),
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Surface_Card)
            .height(60.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .height(16.dp)
                        .widthIn(80.dp)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .height(8.dp)
                        .widthIn(30.dp)
                        .shimmerEffect()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 30.dp)
                        .clip(CircleShape)
                        .height(12.dp)
                        .weight(1f)
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(12.dp)
                        .shimmerEffect()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserListItemShimmerEffectPreview() {
    ChatAppTheme {
        UserListItemShimmerEffect()
    }
}