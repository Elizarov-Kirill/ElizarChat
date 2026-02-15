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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import com.example.elizarchat.data.remote.websocket.WebSocketState
import com.example.elizarchat.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Создаем зависимости
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiManager = remember { ApiManager(context) }
    val webSocketManager = remember { WebSocketManager(context, tokenManager, apiManager) }

    // Подключаем WebSocket к жизненному циклу
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(webSocketManager)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(webSocketManager)
        }
    }

    // Создаем ViewModel с правильными параметрами
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.provideFactory(apiManager, webSocketManager, tokenManager, chatId)
    )

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Состояние для поля ввода
    var messageText by remember { mutableStateOf("") }

    // Получаем ID текущего пользователя
    val currentUserId = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        currentUserId.value = tokenManager.getUserId()?.toIntOrNull() ?: 0
    }

    // Эффект для прокрутки вниз при новых сообщениях
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    // Эффект для отметки сообщений как прочитанных
    LaunchedEffect(state.messages) {
        if (state.messages.isNotEmpty() && currentUserId.value > 0) {
            val unreadMessages = state.messages.filter {
                it.userId != currentUserId.value &&
                        (it.readBy?.contains(currentUserId.value) != true)
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
                        Text(state.chatInfo?.name ?: "Загрузка...")
                        if (state.typingUsers.isNotEmpty()) {
                            Text(
                                text = "печатает...",
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
                    // Статус подключения
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.isLoading && state.messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.error != null && state.messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = state.messages.reversed(),
                            key = { it.id }
                        ) { message ->
                            MessageBubble(
                                message = message,
                                isOwnMessage = message.userId == currentUserId.value,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            if (state.isLoadingMore) {
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

            // Поле ввода сообщения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        viewModel.sendTypingStatus(it.isNotEmpty())
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение...") },
                    shape = RoundedCornerShape(24.dp),
                    enabled = state.connectionStatus is WebSocketState.Connected
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (messageText.isNotBlank() && state.connectionStatus is WebSocketState.Connected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageDto,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
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
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Имя отправителя для групповых чатов
                if (!isOwnMessage && message.userId != 0) {
                    Text(
                        text = "Пользователь ${message.userId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                // Текст сообщения
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Время и статус
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = formatTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )

                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (message.status) {
                                "sending" -> "🕒"
                                "sent" -> "✓"
                                "delivered" -> "✓✓"
                                "read" -> "👁️"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: String?): String {
    if (timestamp.isNullOrEmpty()) return ""

    return try {
        val instant = Instant.parse(timestamp)
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        ""
    }
}