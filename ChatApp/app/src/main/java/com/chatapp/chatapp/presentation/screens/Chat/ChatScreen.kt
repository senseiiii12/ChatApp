package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatInputField
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatItem
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatTopBar
import com.chatapp.chatapp.presentation.screens.Chat.details.DateSeparator
import com.chatapp.chatapp.presentation.screens.Chat.details.MessageItem
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.Outline_Card
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ChatScreen(
    chatId: String,
    currentUser: User,
    otherUser: User,
    navController: NavController,
) {
    val chatViewModel: ChatViewModel = hiltViewModel()

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Surface_Card)

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val isEditing by remember { derivedStateOf { chatViewModel.isEditing } }
    val inputMessage by remember { derivedStateOf { chatViewModel.inputMessage } }
    val newMessageText by remember { derivedStateOf { chatViewModel.newMessageText } }



    LaunchedEffect(Unit) {
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
                    }
                    else {
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
        MessageList(
            currentUser = currentUser ,
            currentUserId = currentUserId,
            otherUser = otherUser,
            chatId = chatId,
            paddingValues = paddingValues ,
        )
    }
}



@Composable
fun MessageList(
    currentUser: User,
    currentUserId: String,
    otherUser: User,
    chatId: String,
    paddingValues: PaddingValues,
) {

    val chatViewModel: ChatViewModel = hiltViewModel()
    val chatItems by chatViewModel.chatItems.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()


    Box(
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
            itemsIndexed(chatItems, key = {_, item ->
                when (item) {
                    is ChatItem.MessageItem -> item.message.messageId
                    is ChatItem.DateSeparatorItem -> item.date
                }
            }) { index, item ->
                when (item) {
                    is ChatItem.MessageItem -> {
                        Log.d("Indexmessage", "${item.message.text} - $index")
                        val isCurrentUser = item.message.userId == currentUserId
                        MessageItem(
                            modifier = Modifier
                                .animateItem()
                                .onGloballyPositioned { layoutCoordinates ->
                                    if (!isCurrentUser && item.message.status != MessageStatus.READ) {
                                        val viewportBounds = listState.layoutInfo.viewportEndOffset
                                        if (layoutCoordinates.positionInParent().y < viewportBounds) {
                                            chatViewModel.markMessageAsRead(
                                                chatId,
                                                item.message.messageId
                                            )
                                        }
                                    }
                                },
                            message = item.message,
                            isCurrentUser = isCurrentUser,
                            currentUser = currentUser,
                            otherUser = otherUser,
                            isEditing = chatViewModel.editingMessageId == item.message.messageId,
                            status = item.message.status,
                            onDelete =  { currentMessage ->
                                if (isCurrentUser) {
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
                    is ChatItem.DateSeparatorItem -> {
                        Log.d("Indexmessage", "${item.date} - $index")
                        DateSeparator(date = item.date)
                    }
                }
            }
        }

        FAB(
            modifier = Modifier.align(Alignment.BottomEnd) ,
            scope = scope,
            listState = listState
        )

        ScrollToEndList(
            chatItems = chatItems,
            listState = listState
        )
    }
}

@Composable
fun FAB(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    listState: LazyListState
) {
    val isVisibleFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisibleFab,
        enter = fadeIn(initialAlpha = 0f) + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        IconButton(
            modifier = Modifier
                .padding(end = 10.dp, bottom = 10.dp)
                .size(35.dp)
                .clip(CircleShape)
                .shadow(5.dp)
                .background(Outline_Card),
            onClick = {
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = ChatText
            )
        }

    }
}

@Composable
fun ScrollToEndList(
    chatItems: List<ChatItem>,
    listState: LazyListState,
){
    val isVisibleFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    if (!isVisibleFab){
        LaunchedEffect(chatItems.size) {
            delay(100)
            if (chatItems.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
    }
}






