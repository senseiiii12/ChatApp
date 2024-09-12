package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatInputField
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatTopBar
import com.chatapp.chatapp.presentation.screens.Chat.details.MessageItem
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatId: String,
    currentUser: User,
    otherUser: User,
    navController: NavController,
    chatViewModel: ChatViewModel
) {


    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Surface_Card)

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val messages by chatViewModel.messages.collectAsState()
    val isEditing by remember { derivedStateOf { chatViewModel.isEditing } }
    val inputMessage by remember { derivedStateOf { chatViewModel.inputMessage } }
    val newMessageText by remember { derivedStateOf { chatViewModel.newMessageText } }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        chatViewModel.listenForMessages(chatId)
    }

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            ChatTopBar(
                otherUser = otherUser,
                navController = navController,
            )
        },
        bottomBar = {
            ChatInputField(
                inputMessage = inputMessage,
                newMessageText = newMessageText,
                isEditing = isEditing,
                onInputMessageChange = chatViewModel::updateInputMessageText,
                onNewMessageChange = chatViewModel::updateNewMessageText,
                onSendMessage = {
                    if (isEditing && newMessageText.isNotEmpty()) {
                        chatViewModel.onSaveEditMessage(chatId, chatViewModel.editingMessageId, newMessageText)
                        chatViewModel.resetEditMode()
                    } else {
                        if (inputMessage.isNotEmpty()){
                            chatViewModel.sendMessage(chatId, currentUserId, inputMessage)
                            chatViewModel.resetInputMessage()
                        }
                    }
                },
                onCancelEdit = chatViewModel::resetEditMode
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.Bottom
            ) {
                itemsIndexed(messages, key = { _, msg -> msg.messageId }) { index, message ->
                    val previousMessage = if (index < messages.size - 1) messages[index + 1] else null

                    Column {
                        if (shouldShowDateSeparator(previousMessage, message)) {
                            DateSeparator(message.timestamp)
                        }
                        MessageItem(
                            modifier = Modifier
                                .animateItemPlacement()
                                .onGloballyPositioned { layoutCoordinates ->
                                    if (message.userId != currentUserId && message.status != MessageStatus.READ) {
                                        val viewportBounds = listState.layoutInfo.viewportEndOffset
                                        if (layoutCoordinates.positionInParent().y < viewportBounds) {
                                            chatViewModel.markMessageAsRead(chatId, message.messageId)
                                        }
                                    }
                                },
                            message = message,
                            isCurrentUser = message.userId == currentUserId,
                            currentUser = currentUser,
                            otherUser = otherUser,
                            isEditing = chatViewModel.editingMessageId == message.messageId,
                            status = message.status,
                            onDelete = { currentMessage ->
                                if (message.userId == currentUserId) {
                                    chatViewModel.deleteMessage(chatId, currentMessage.messageId)
                                }
                            },
                            onEditMessage = { currentMessage ->
                                chatViewModel.initEditMessageState(
                                    isEditing = true,
                                    newMessageText = currentMessage.text,
                                    editingMessageId = currentMessage.messageId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(messages.size) {
        delay(200)
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
}

@Composable
fun DateSeparator(timestamp: Date) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH) }
    val dateText = remember { dateFormat.format(timestamp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = DarkGray_2
        )
        Text(
            text = dateText,
            modifier = Modifier.padding(horizontal = 8.dp),
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = DarkGray_1,
            fontSize = 10.sp
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = DarkGray_2,
        )
    }
}

fun shouldShowDateSeparator(previousMessage: Message?, currentMessage: Message): Boolean {
    if (previousMessage == null) return true

    val previousDate = previousMessage.timestamp.toStartOfDay()
    val currentDate = currentMessage.timestamp.toStartOfDay()

    return previousDate != currentDate
}

fun Date.toStartOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}







