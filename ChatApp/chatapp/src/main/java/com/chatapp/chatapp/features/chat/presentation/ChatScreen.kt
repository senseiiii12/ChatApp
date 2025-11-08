package com.chatapp.chatapp.features.chat.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat.presentation.details.ChatItem
import com.chatapp.chatapp.features.chat.presentation.details.MessageDateSeparatorItem
import com.chatapp.chatapp.features.chat.presentation.details.MessageItem
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputField
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputFieldState
import com.chatapp.chatapp.features.chat.presentation.details.topbar.ChatTopBar
import com.chatapp.chatapp.features.chat.presentation.details.topbar.TopMenuState
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.MyCustomTypography
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
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val chatInputFieldState = chatViewModel.chatInputFieldState.collectAsState()
    val topMenuState = chatViewModel.topMenuState.collectAsState()
    val operationError by chatViewModel.operationError.collectAsState()

    // Инициализация слушателя сообщений
    LaunchedEffect(chatId) {
        chatViewModel.listenForMessagesInChat(chatId, listState)
    }

    // Очистка ресурсов при выходе из экрана
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.stopListeningToMessages()
        }
    }

    // Обработка ошибок
    LaunchedEffect(operationError) {
        operationError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
            chatViewModel.clearError()
        }
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
                    if (copiedMessage.isNotEmpty()) {
                        toastState.showToast("Скопировано: ${copiedMessage.take(30)}...")
                    }
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
private fun MyBackHandler(
    topMenuState: State<TopMenuState>,
    chatInputFieldState: State<ChatInputFieldState>,
    onDisableTopMenu: () -> Unit,
    onDisableEditMode: () -> Unit,
) {
    val isMenuOpen = topMenuState.value.isOpenTopMenu
    val isEditing = chatInputFieldState.value.isEditingMessage

    BackHandler(enabled = isMenuOpen || isEditing) {
        when {
            isMenuOpen -> onDisableTopMenu()
            isEditing -> onDisableEditMode()
        }
    }
}

@Composable
private fun MessageList(
    chatViewModel: ChatViewModel,
    currentUser: User,
    currentUserId: String,
    otherUser: User,
    chatId: String,
    paddingValues: PaddingValues,
    listState: LazyListState,
    topMenuState: State<TopMenuState>
) {
    val chatItems by chatViewModel.chatItems.collectAsState()
    val unreadMessagesCount by chatViewModel.unreadMessagesCount.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom
        ) {
            itemsIndexed(
                items = chatItems,
                key = { index, item ->
                    when (item) {
                        is ChatItem.MessageItem -> item.message.messageId
                        is ChatItem.DateSeparatorItem -> "$index-${item.date}"
                    }
                }
            ) { index, item ->
                when (item) {
                    is ChatItem.MessageItem -> {
                        MessageItemWrapper(
                            item = item,
                            currentUserId = currentUserId,
                            currentUser = currentUser,
                            otherUser = otherUser,
                            chatId = chatId,
                            listState = listState,
                            topMenuState = topMenuState,
                            chatViewModel = chatViewModel
                        )
                    }
                    is ChatItem.DateSeparatorItem -> {
                        MessageDateSeparatorItem(date = item.date)
                    }
                }
            }
        }

        ScrollToEndList(
            chatItems = chatItems,
            listState = listState
        )

        FAB(
            modifier = Modifier.align(Alignment.BottomEnd),
            scope = scope,
            listState = listState,
            unreadMessagesCount = unreadMessagesCount,
            onScrollToBottom = chatViewModel::resetUnreadMessagesCount
        )
    }
}

@Composable
private fun MessageItemWrapper(
    item: ChatItem.MessageItem,
    currentUserId: String,
    currentUser: User,
    otherUser: User,
    chatId: String,
    listState: LazyListState,
    topMenuState: State<TopMenuState>,
    chatViewModel: ChatViewModel
) {
    val isCurrentUser = item.message.userId == currentUserId
    val isSelected = topMenuState.value.listSelectedMessages.contains(item.message)

    MessageItem(
        modifier = Modifier
            .animateContentSize()
            .onGloballyPositioned { layoutCoordinates ->
                // Помечаем сообщение как прочитанное когда оно попадает в область видимости
                if (!isCurrentUser && item.message.status != MessageStatus.READ) {
                    val viewportBounds = listState.layoutInfo.viewportEndOffset
                    val messagePosition = layoutCoordinates.positionInParent().y

                    if (messagePosition < viewportBounds) {
                        chatViewModel.markMessageAsRead(chatId, item.message.messageId)
                        chatViewModel.deleteUnreadMessageToScroll(item.message)
                    }
                }
            },
        message = item.message,
        isCurrentUser = isCurrentUser,
        currentUser = currentUser,
        otherUser = otherUser,
        isEditing = isSelected,
        status = item.message.status,
        onOpenTopMenu = { currentMessage ->
            chatViewModel.toggleMessageSelection(currentMessage)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FAB(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    listState: LazyListState,
    unreadMessagesCount: Int,
    onScrollToBottom: () -> Unit
) {
    val isVisibleFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
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
                    contentDescription = "Прокрутить вниз",
                    tint = ChatText
                )
            }

            UnreadBadge(
                modifier = Modifier.align(Alignment.TopCenter),
                unreadCount = unreadMessagesCount
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnreadBadge(
    modifier: Modifier = Modifier,
    unreadCount: Int
) {
    if (unreadCount <= 0) return

    val animatedUnreadCount by animateIntAsState(
        targetValue = unreadCount,
        animationSpec = tween(
            durationMillis = 50,
            easing = FastOutSlowInEasing
        ),
        label = "unread_count_animation"
    )

    Badge(
        modifier = modifier.offset(x = (-10).dp, y = (-8).dp),
        containerColor = PrimaryPurple
    ) {
        AnimatedContent(
            targetState = animatedUnreadCount,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }
            },
            label = "unread_badge_content"
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                color = Color.White,
                style = MyCustomTypography.Bold_12
            )
        }
    }
}

@Composable
private fun ScrollToEndList(
    chatItems: List<ChatItem>,
    listState: LazyListState,
) {
    val messageItemSize = chatItems.count { it is ChatItem.MessageItem }

    val isAtBottom = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            firstVisibleItemIndex <= 2
        }
    }

    LaunchedEffect(messageItemSize) {
        if (chatItems.isNotEmpty() && isAtBottom.value) {
            listState.animateScrollToItem(0)
        }
    }
}