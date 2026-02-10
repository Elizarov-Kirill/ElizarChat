package com.example.elizarchat.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.ui.viewmodels.CreateChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatScreen(
    onNavigateToChat: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Создаем зависимости вручную
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiManager = remember { ApiManager(context) }

    // Используем ViewModel с фабрикой
    val viewModel: CreateChatViewModel = viewModel(
        factory = CreateChatViewModel.provideFactory(apiManager, tokenManager)
    )

    val state by viewModel.state.collectAsState()

    // Навигация при успешном создании чата
    LaunchedEffect(state.createdChat) {
        state.createdChat?.let { chat ->
            onNavigateToChat(chat.id)
            viewModel.clearCreatedChat()
        }
    }

    // Диалог для ввода имени чата
    if (state.showNameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideNameDialog() },
            title = { Text("Enter chat name") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.chatName,
                        onValueChange = { viewModel.updateChatName(it) },
                        label = { Text("Chat name (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.selectedUsers.size > 1) {
                        Text("Chat type:", style = MaterialTheme.typography.labelMedium)
                        Row {
                            FilterChip(
                                selected = state.chatType == "group",
                                onClick = { viewModel.updateChatType("group") },
                                label = { Text("Group") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = state.chatType == "channel",
                                onClick = { viewModel.updateChatType("channel") },
                                label = { Text("Channel") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideNameDialog()
                        viewModel.createChat()
                    },
                    enabled = !state.isCreating
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideNameDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.selectedUsers.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.showNameDialog() },
                            enabled = !state.isCreating
                        ) {
                            if (state.isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = "Create")
                            }
                        }
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
            // Выбранные пользователи
            if (state.selectedUsers.isNotEmpty()) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Selected users (${state.selectedUsers.size}):",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(state.selectedUsers) { user ->
                                SelectedUserItem(
                                    user = user,
                                    onRemove = { viewModel.removeUser(user.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Поиск пользователей
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search users") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Результаты поиска
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isSearching -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.error != null -> {
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
                                Button(onClick = { viewModel.clearError() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.searchResults.isEmpty() && state.searchQuery.length >= 2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "No results",
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No users found")
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.searchResults) { user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = { viewModel.addUser(user) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(
    user: com.example.elizarchat.data.remote.dto.UserDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium
                )

                user.displayName?.let { displayName ->
                    if (displayName != user.username) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (user.isOnline) {
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                Icons.Default.Add,
                contentDescription = "Add user",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SelectedUserItem(
    user: com.example.elizarchat.data.remote.dto.UserDto,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}