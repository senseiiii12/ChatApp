package com.chatapp.chatapp.presentation.screens.Chat.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RichTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_Card
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message,
    isCurrentUser: Boolean,
    isEditing: Boolean,
    status: MessageStatus,
    onDelete: (String) -> Unit,
    onEditMessage: (Message) -> Unit,
) {

    val currentUserColor = remember {
        Brush.linearGradient(listOf(Surface_Card.copy(alpha = 0.8f), Surface_Card))
    }
    val otherUserColor = remember {
        Brush.linearGradient(listOf(PrimaryPurple.copy(alpha = 0.6f), PrimaryPurple))
    }
    val backgroundColor = if (isCurrentUser) currentUserColor else otherUserColor
    val timeAlignment = if (isCurrentUser) Alignment.End else Alignment.Start

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (!isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        RichTooltipBox(
            colors = TooltipDefaults.richTooltipColors(
                containerColor = DarkGray_2
            ),
            shape = RoundedCornerShape(16.dp),
            text = {
                Row(horizontalArrangement = Arrangement.SpaceAround) {
                    Image(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onEditMessage(message) }
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = null,
                    )
                    Image(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onDelete(message.messageId) }
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = null,
                    )
                }
            }
        ) {
            val anchor_toolTip: Modifier =
                if (isCurrentUser && !isEditing) Modifier.tooltipAnchor() else Modifier
            Column(
                modifier = anchor_toolTip
                    .clip(RoundedCornerShape(15.dp))
                    .widthIn(min = 20.dp, max = 200.dp)
                    .background(backgroundColor)
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 6.dp)
                    .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
            ) {
                AnimatedVisibility(visible = isEditing) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .size(12.dp),
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = ChatText
                    )
                }
                Text(
                    text = message.text,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                    color = ChatText,
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestampToDate(message.timestamp),
                        fontSize = 8.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                        color = ChatText.copy(alpha = 0.82f),
                    )
                    AnimatedVisibility(visible = isCurrentUser) {
                        when (status) {
                            MessageStatus.DELIVERED -> {
                                Icon(
                                    modifier = Modifier.size(12.dp),
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryPurple
                                )
                            }
                            MessageStatus.READ -> {
                                Image(
                                    modifier = Modifier.size(12.dp),
                                    painter = painterResource(id = R.drawable.ic_double_check),
                                    contentDescription = null,
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageItemPreview() {
    ChatAppTheme {
        MessageItem(
            message = Message(
                userId = "BIsXDQm57KgWPqgYIhzUwXnfBkZ2",
                text = "Hello",
                timestamp = Date(0),
                messageId = "b8224c6e-9786-4cf8-8692-5189e47c1b7d"
            ),
            isCurrentUser = true,
            isEditing = true,
            status = MessageStatus.READ,
            onDelete = {},
            onEditMessage = {},
        )
    }
}

fun formatTimestampToDate(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}