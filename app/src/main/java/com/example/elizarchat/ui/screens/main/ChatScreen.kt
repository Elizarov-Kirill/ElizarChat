package com.example.elizarchat.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.websocket.WebSocketState
import com.example.elizarchat.di.ServiceLocator
import com.example.elizarchat.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    chatId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // ПОЛУЧАЕМ СИНГЛТОНЫ ИЗ SERVICE LOCATOR
    val tokenManager = remember { ServiceLocator.getTokenManager(context) }
    val apiManager = remember { ServiceLocator.getApiManager(context) }
    val webSocketManager = remember { ServiceLocator.getWebSocketManager(context) }

    // Регистрируем LifecycleObserver
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(webSocketManager)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(webSocketManager)
        }
    }

    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.provideFactory(
            apiManager = apiManager,
            webSocketManager = webSocketManager,
            tokenManager = tokenManager,
            chatId = chatId
        )
    )

    val state by viewModel.state.collectAsState()
    val usersCache by viewModel.usersCache.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }

    // ИСПРАВЛЕНО: получаем userId напрямую как Int
    val currentUserId = remember {
        tokenManager.getUserIdSync()?.toIntOrNull() ?: 0
    }

    val isImeVisible = WindowInsets.isImeVisible
    val isConnected = state.connectionStatus is WebSocketState.Connected

    // Прокрутка к последнему сообщению
    LaunchedEffect(state.messages.size, isImeVisible) {
        if (state.messages.isNotEmpty()) {
            delay(if (isImeVisible) 100 else 50)
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    // Прокрутка при первом открытии
    LaunchedEffect(state.messages.isNotEmpty()) {
        if (state.messages.isNotEmpty()) {
            delay(150)
            coroutineScope.launch {
                listState.scrollToItem(state.messages.size - 1)
            }
        }
    }

    // Отметка прочитанных сообщений
    LaunchedEffect(state.messages) {
        if (state.messages.isNotEmpty() && currentUserId > 0) {
            val unreadMessages = state.messages.filter {
                it.userId != currentUserId &&
                        (it.readBy?.contains(currentUserId) != true)
            }
            if (unreadMessages.isNotEmpty()) {
                viewModel.markMessagesAsRead(unreadMessages.map { it.id })
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.chatInfo?.name ?: "Загрузка...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (state.typingUsers.isNotEmpty()) {
                            Text(
                                text = if (state.typingUsers.size == 1) "печатает..." else "печатают...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Индикатор подключения WebSocket
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (state.connectionStatus) {
                                    is WebSocketState.Connected -> Color.Green
                                    is WebSocketState.Connecting -> Color.Yellow
                                    else -> Color.Red
                                }
                            )
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Список сообщений
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
            ) {
                when {
                    state.isLoading && state.messages.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    state.error != null && state.messages.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ошибка: ${state.error}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { viewModel.loadMessages(refresh = true) }) {
                                        Text("Повторить")
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        items(
                            items = state.messages,
                            key = { it.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                isOwnMessage = message.userId == currentUserId,
                                modifier = Modifier.fillMaxWidth(),
                                usersCache = usersCache
                            )
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }

            // Поле ввода
            MessageInputCorrect(
                messageText = messageText,
                onMessageChange = { newText ->
                    messageText = newText
                    viewModel.sendTypingStatus(newText.isNotEmpty())
                },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                isConnected = isConnected,
                isLoading = state.isSending
            )
        }
    }
}

@Composable
fun MessageInputCorrect(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isConnected: Boolean,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    placeholder = {
                        Text(
                            text = if (isConnected) "Введите сообщение..." else "Подключение...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    enabled = isConnected && !isLoading,
                    singleLine = false,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = when {
                        !isConnected -> MaterialTheme.colorScheme.surfaceVariant
                        messageText.isNotBlank() -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    }
                ) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = if (messageText.isNotBlank()) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        else -> {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Отправить",
                                tint = if (messageText.isNotBlank() && isConnected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageDto,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier,
    usersCache: Map<Int, com.example.elizarchat.data.remote.dto.UserDto> = emptyMap()
) {
    val backgroundColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    val senderName = if (!isOwnMessage && message.userId != 0) {
        usersCache[message.userId]?.displayName
            ?: usersCache[message.userId]?.username
            ?: if (message.sender?.displayName != null) message.sender.displayName
            else if (message.sender?.username != null) message.sender.username
            else null
    } else null

    Row(
        modifier = modifier,
        horizontalArrangement = if (isOwnMessage) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (senderName != null && !isOwnMessage) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.9
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = formatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.6f),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85
                    )

                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getMessageStatusIcon(message.status),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun getMessageStatusIcon(status: String?): String {
    return when (status) {
        "sending" -> "🕒"
        "sent" -> "✓"
        "delivered" -> "✓✓"
        "read" -> "👁️"
        else -> ""
    }
}

private fun formatTime(timestamp: String?): String {
    if (timestamp.isNullOrEmpty()) return ""

    return try {
        val instant = Instant.parse(timestamp)
        val now = Instant.now()
        val diff = java.time.Duration.between(instant, now)

        when {
            diff.toMinutes() < 1 -> "только что"
            diff.toHours() < 1 -> "${diff.toMinutes()} мин"
            diff.toDays() < 1 -> {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
            else -> {
                val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            }
        }
    } catch (e: Exception) {
        ""
    }
}