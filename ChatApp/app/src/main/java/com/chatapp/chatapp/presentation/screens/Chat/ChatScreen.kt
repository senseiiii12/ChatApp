package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatInputField
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatTopBar
import com.chatapp.chatapp.presentation.screens.Chat.details.MessageItem
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatId: String,
    otherUser: User,
    navController: NavController,
    chatViewModel: ChatViewModel
) {


    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Surface_Card)

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                items(messages, key = { msg -> msg.messageId }) { message ->
                    val isCurrentMessageEditing = chatViewModel.editingMessageId == message.messageId
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
                        isEditing = isCurrentMessageEditing,
                        status = message.status,
                        onDelete = { messageId ->
                            if (message.userId == currentUserId) {
                                chatViewModel.deleteMessage(chatId, messageId)
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
    LaunchedEffect(messages.size) {
        delay(200)
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
}









