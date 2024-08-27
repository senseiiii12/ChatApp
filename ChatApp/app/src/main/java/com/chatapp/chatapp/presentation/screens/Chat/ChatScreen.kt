package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatInputField
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatTopBar
import com.chatapp.chatapp.presentation.screens.Chat.details.MessageItem
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
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

    Log.d("OtherUser", otherUser.toString())
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









