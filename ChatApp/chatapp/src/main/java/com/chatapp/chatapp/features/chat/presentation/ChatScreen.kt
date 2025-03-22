package com.chatapp.chatapp.features.chat.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.presentation.details.ChatItem
import com.chatapp.chatapp.features.chat.presentation.details.MessageDateSeparatorItem
import com.chatapp.chatapp.features.chat.presentation.details.MessageItem
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputField
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputFieldState
import com.chatapp.chatapp.features.chat.presentation.details.topbar.ChatTopBar
import com.chatapp.chatapp.features.chat.presentation.details.topbar.TopMenuState
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.Outline_Card
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.CustomToast.ToastHost
import com.chatapp.chatapp.util.CustomToast.ToastState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent


@Composable
fun ChatScreen(
    chatId: String,
    currentUser: User,
    otherUser: User,
    navController: NavController,
    chatViewModel: ChatViewModel,
    usersViewModel: UsersViewModel
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(SecondaryBackground)

    val context = LocalContext.current
    val toastState = remember { ToastState() }
    val listState = rememberLazyListState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val chatInputFieldState = chatViewModel.chatInputFieldState.collectAsState()
    val topMenuState = chatViewModel.topMenuState.collectAsState()

    LaunchedEffect(Unit) {
        chatViewModel.listenForMessagesInChat(chatId, listState)
    }

    MyBackHandler(
        topMenuState = topMenuState,
        chatInputFieldState = chatInputFieldState,
        onDisableTopMenu = chatViewModel::resetStateTopMenu,
        onDisableEditMode = chatViewModel::resetEditMode
    )


    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            ChatTopBar(
                otherUser = otherUser,
                stateTopMenu = topMenuState,
                usersViewModel = usersViewModel,
                onBack = { navController.popBackStack() },
                onCloseMenu = {
                    chatViewModel.stateTopMenuMessage(false)
                    chatViewModel.clearSelectedMessages()
                },
                onDeleteMessage = { messageList ->
                    chatViewModel.deleteMessage(chatId, messageList)
                },
                onEditMessage = { newMessage, currentMessage ->
                    chatViewModel.initEditMessageState(true, newMessage, currentMessage)
                    chatViewModel.stateTopMenuMessage(false)
                },
                onCopyMessage = { messageList ->
                    val copiedMessage = chatViewModel.copySelectedMessage(context, messageList)
                    toastState.showToast("Copy: $copiedMessage")
                }
            )
        },
        bottomBar = {
            ChatInputField(
                state = chatInputFieldState,
                onInputMessageChange = chatViewModel::updateDefaultInputMessage,
                onNewMessageChange = chatViewModel::updateEditingInputMessage,
                onSendMessage = { state ->
                    chatViewModel.handleSendMessage(
                        currentChatId = chatId,
                        currentUserId = currentUserId,
                        sendState = state
                    )
                },
                onCancelEdit = chatViewModel::resetEditMode
            )
        }
    ) { paddingValues ->
        MessageList(
            chatViewModel = chatViewModel,
            currentUser = currentUser,
            currentUserId = currentUserId,
            otherUser = otherUser,
            chatId = chatId,
            paddingValues = paddingValues,
            listState = listState,
            topMenuState = topMenuState
        )
        ToastHost(toastState)
    }
}

@Composable
fun MyBackHandler(
    topMenuState: State<TopMenuState>,
    chatInputFieldState: State<ChatInputFieldState>,
    onDisableTopMenu: () -> Unit,
    onDisableEditMode: () -> Unit,
) {
    BackHandler(enabled = topMenuState.value.isOpenTopMenu || chatInputFieldState.value.isEditingMessage) {
        if (topMenuState.value.isOpenTopMenu) onDisableTopMenu()
        else if (chatInputFieldState.value.isEditingMessage) onDisableEditMode()
    }
}


@Composable
fun MessageList(
    chatViewModel: ChatViewModel,
    currentUser: User,
    currentUserId: String,
    otherUser: User,
    chatId: String,
    paddingValues: PaddingValues,
    listState: LazyListState,
    topMenuState: State<TopMenuState>
) {

    val chatItems = chatViewModel.chatItems.collectAsState()
    val unreadMessagesCount by chatViewModel.unreadMessagesCount.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom
        ) {
            itemsIndexed(chatItems.value, key = { _, item ->
                when (item) {
                    is ChatItem.MessageItem -> item.message.messageId
                    is ChatItem.DateSeparatorItem -> item.date
                }
            }) { index, item ->
                when (item) {
                    is ChatItem.MessageItem -> {
                        val isCurrentUser = item.message.userId == currentUserId
                        MessageItem(
                            modifier = Modifier
                                .animateContentSize()
                                .onGloballyPositioned { layoutCoordinates ->
                                    if (!isCurrentUser && item.message.status != MessageStatus.READ) {
                                        val viewportBounds = listState.layoutInfo.viewportEndOffset
                                        if (layoutCoordinates.positionInParent().y < viewportBounds) {
                                            chatViewModel.markMessageAsRead(
                                                chatId,
                                                item.message.messageId
                                            )
                                            chatViewModel.deleteUnreadMessageToScroll(item.message)
                                        }
                                    }
                                },
                            message = item.message,
                            isCurrentUser = isCurrentUser,
                            currentUser = currentUser,
                            otherUser = otherUser,
                            isEditing = topMenuState.value.listSelectedMessages.contains(item.message),
                            status = item.message.status,
                            onOpenTopMenu = { currentMessage ->
                                chatViewModel.toggleMessageSelection(currentMessage)
                            }
                        )
                    }
                    is ChatItem.DateSeparatorItem -> {
                        MessageDateSeparatorItem(date = item.date)
                    }
                }
            }
        }

        ScrollToEndList(
            chatItems = chatItems.value,
            listState = listState
        )

        FAB(
            modifier = Modifier.align(Alignment.BottomEnd),
            scope = scope,
            listState = listState,
            unreadMessagesCount = unreadMessagesCount,
            onScrollToBottom = { chatViewModel.resetUnreadMessagesCount() }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAB(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    listState: LazyListState,
    unreadMessagesCount: Int,
    onScrollToBottom: () -> Unit
) {
    val isVisibleFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = isVisibleFab,
        enter = fadeIn(initialAlpha = 0f),
        exit = fadeOut()
    ) {
        Box {
            IconButton(
                modifier = Modifier
                    .padding(end = 20.dp, bottom = 10.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .shadow(5.dp)
                    .background(Outline_Card),
                onClick = {
                    scope.launch {
                        onScrollToBottom()
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

            if (unreadMessagesCount > 0) {
                val animatedUnreadCount by animateIntAsState(
                    targetValue = unreadMessagesCount,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )

                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = (-4).dp),
                    containerColor = PrimaryPurple
                ) {
                    Text(
                        text = animatedUnreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollToEndList(
    chatItems: List<ChatItem>,
    listState: LazyListState,
) {
    val isVisibleFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }
    if (!isVisibleFab) {
        LaunchedEffect(chatItems.size) {
            if (chatItems.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
    }
}






