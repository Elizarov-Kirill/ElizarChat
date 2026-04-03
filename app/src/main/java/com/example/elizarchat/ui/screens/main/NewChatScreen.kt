package com.example.elizarchat.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
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
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.ui.viewmodels.NewChatViewModel
import com.example.elizarchat.ui.viewmodels.GroupChatState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateToChat: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiManager = remember { ApiManager(context) }

    val viewModel: NewChatViewModel = viewModel(
        factory = NewChatViewModel.provideFactory(apiManager, tokenManager)
    )

    val state by viewModel.state.collectAsState()
    val groupState by viewModel.groupChatState.collectAsState()

    var showGroupChatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigationTarget.collect { chatId ->
            if (chatId != null) {
                onNavigateToChat(chatId)
                viewModel.clearNavigationTarget()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый чат") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGroupChatDialog = true },
                        enabled = !state.isCreatingChat
                    ) {
                        Icon(Icons.Default.Group, contentDescription = "Создать групповой чат")
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
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Поиск пользователей") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.isCreatingChat) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.creatingChatMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

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
                                    text = "Ошибка: ${state.error}",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { viewModel.clearError() }) {
                                    Text("Повторить")
                                }
                            }
                        }
                    }

                    state.users.isEmpty() && state.searchQuery.length >= 2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Нет результатов",
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Пользователи не найдены")
                            }
                        }
                    }

                    state.users.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.users, key = { it.id }) { user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = {
                                        viewModel.startPrivateChat(user.id)
                                    }
                                )
                            }
                        }
                    }

                    state.searchQuery.length < 2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Поиск",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Введите минимум 2 символа для поиска",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Нажмите на пользователя для личного чата",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showGroupChatDialog = true }
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Создать групповой чат")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupChatDialog) {
        GroupChatDialog(
            state = groupState,
            onDismiss = {
                showGroupChatDialog = false
                viewModel.resetGroupChat()
            },
            onAddUser = { viewModel.addUserToGroup(it) },
            onRemoveUser = { viewModel.removeUserFromGroup(it) },
            onUpdateChatName = { viewModel.updateGroupChatName(it) },
            onCreateGroup = { viewModel.createGroupChat() }
        )
    }
}

@Composable
fun UserSearchItem(
    user: UserDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Аватар",
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
                        text = "В сети",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Личный чат",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun GroupChatDialog(
    state: GroupChatState,
    onDismiss: () -> Unit,
    onAddUser: (UserDto) -> Unit,
    onRemoveUser: (Int) -> Unit,
    onUpdateChatName: (String) -> Unit,
    onCreateGroup: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiManager = remember { ApiManager(context) }

    suspend fun searchUsers(query: String): List<UserDto> {
        return try {
            val response = apiManager.searchUsers(query = query)
            if (response.isSuccessful) {
                val usersResponse = response.body()
                usersResponse?.users?.filter { it.id != (tokenManager.getUserId()?.toIntOrNull() ?: 0) } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            searchResults = searchUsers(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp),
        title = { Text("Создание группового чата") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = state.chatName,
                    onValueChange = onUpdateChatName,
                    label = { Text("Название группы") },
                    placeholder = { Text("Введите название группы") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.chatName.isBlank() && state.error != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.selectedUsers.isNotEmpty()) {
                    Text(
                        text = "Выбранные участники (${state.selectedUsers.size}):",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(state.selectedUsers) { user ->
                            GroupSelectedUserItem(
                                user = user,
                                onRemove = { onRemoveUser(user.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Добавить участников:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск пользователей") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { user ->
                            if (!state.selectedUsers.any { it.id == user.id }) {
                                UserAddItem(
                                    user = user,
                                    onAdd = { onAddUser(user) }
                                )
                            }
                        }
                    }
                } else if (searchQuery.length >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Пользователи не найдены")
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateGroup,
                enabled = state.selectedUsers.size >= 2 && !state.isCreating && state.chatName.isNotBlank()
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Создать группу")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun GroupSelectedUserItem(
    user: UserDto,
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
                    .size(32.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Аватар",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                    contentDescription = "Удалить",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun UserAddItem(
    user: UserDto,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onAdd)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Аватар",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium
                )
                user.displayName?.let { displayName ->
                    if (displayName != user.username) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Icon(
                Icons.Default.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}