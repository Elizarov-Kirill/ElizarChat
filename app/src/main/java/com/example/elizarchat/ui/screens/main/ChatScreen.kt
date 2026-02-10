package com.example.elizarchat.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.di.ServiceLocator
import com.example.elizarchat.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Создаем зависимости вручную
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiManager = remember { ApiManager(context) }
    val webSocketManager = remember { ServiceLocator.getWebSocketManager(context) }

    // Используем ViewModel с фабрикой
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.provideFactory(apiManager, tokenManager, webSocketManager)
    )

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Установка chatId при первом отображении
    LaunchedEffect(chatId) {
        viewModel.setChatId(chatId)
    }

    // Автопрокрутка при новых сообщениях
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Отметка как прочитанного при открытии чата
    LaunchedEffect(state.isLoaded) {
        if (state.isLoaded) {
            viewModel.markAsRead()
        }
    }

    val webSocketConnectionStatus = remember {
        derivedStateOf {
            when {
                state.isConnectedToWebSocket -> "🟢 Online"
                state.isLoading -> "🔄 Connecting..."
                else -> "🔴 Offline"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.chat?.name ?: "Chat",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = webSocketConnectionStatus.value,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        // Показываем индикаторы печатания
                        if (state.typingUsers.isNotEmpty()) {
                            Text(
                                text = "Someone is typing...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
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
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.error != null && state.messages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error: ${state.error}",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.refreshMessages() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.messages.isEmpty() && state.isLoaded -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "No messages",
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No messages yet")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Send the first message!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            items(state.messages.reversed(), key = { it.id }) { message ->
                                MessageBubble(
                                    message = message,
                                    isOwnMessage = state.currentUserId == message.senderId,
                                    onRetrySend = {
                                        if (message.status == "error") {
                                            viewModel.retrySendMessage(message)
                                        }
                                    }
                                )
                            }

                            item {
                                if (state.isLoading && state.messages.isNotEmpty()) {
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
            }

            // Поле ввода сообщения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.newMessageText,
                    onValueChange = { viewModel.updateMessageText(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = false,
                    maxLines = 3,
                    trailingIcon = {
                        if (state.newMessageText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                enabled = !state.isSending
                            ) {
                                if (state.isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { /* TODO: Attach file */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: com.example.elizarchat.data.remote.dto.MessageDto,
    isOwnMessage: Boolean,
    onRetrySend: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                    )
                )
                .background(
                    when {
                        message.status == "error" -> MaterialTheme.colorScheme.errorContainer
                        isOwnMessage -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        message.status == "error" -> MaterialTheme.colorScheme.onErrorContainer
                        isOwnMessage -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                message.createdAt?.let { createdAt ->
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                message.status == "error" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                isOwnMessage -> Color.White.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )

                        if (isOwnMessage) {
                            Spacer(modifier = Modifier.width(4.dp))
                            message.status?.let { status ->
                                when (status) {
                                    "error" -> {
                                        IconButton(
                                            onClick = onRetrySend,
                                            modifier = Modifier.size(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Retry",
                                                tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = when (status) {
                                                "sent" -> Icons.Default.Done
                                                "delivered" -> Icons.Default.DoneAll
                                                "read" -> Icons.Default.DoneAll
                                                else -> Icons.Default.Schedule
                                            },
                                            contentDescription = "Status",
                                            modifier = Modifier.size(12.dp),
                                            tint = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMessageTime(isoTime: String): String {
    return try {
        // Пример: "2026-02-08T11:25:37.874Z" → "11:25"
        isoTime.substring(11, 16)
    } catch (e: Exception) {
        isoTime.take(5)
    }
}