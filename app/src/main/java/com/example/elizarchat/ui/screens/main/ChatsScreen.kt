package com.example.elizarchat.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.getElizarChatApplication
import com.example.elizarchat.ui.viewmodels.ChatsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onLogout: () -> Unit
) {
    println("🚀 DEBUG ChatsScreen: Composable создается")

    val application = getElizarChatApplication()

    // Используем синхронные методы в Composable
    val userId = remember {
        application.tokenManager.getUserIdSync()
    }

    println("🔄 DEBUG ChatsScreen: userId загружен: $userId")

    val viewModel: ChatsViewModel = viewModel(
        factory = ChatsViewModel.provideFactory(
            apiManager = application.apiManager,
            tokenManager = application.tokenManager
        )
    )

    val state by viewModel.state.collectAsState()

    println("📊 DEBUG ChatsScreen: Текущее состояние - " +
            "isLoggedIn=${state.isLoggedIn}, " +
            "isLoading=${state.isLoading}, " +
            "error=${state.error}")

    // Если пользователь не авторизован, переходим на логин
    LaunchedEffect(state.isLoggedIn) {
        println("🔄 DEBUG ChatsScreen: LaunchedEffect isLoggedIn=${state.isLoggedIn}")
        if (!state.isLoggedIn && !state.isLoading) {
            println("🔙 DEBUG ChatsScreen: Пользователь не авторизован, вызываем onLogout()")
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Чаты") },
                actions = {
                    // Кнопка выхода
                    IconButton(
                        onClick = {
                            println("🚪 DEBUG ChatsScreen: Нажата кнопка выхода")
                            viewModel.logout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выход"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> {
                    println("⏳ DEBUG ChatsScreen: Отображение загрузки")
                    CircularProgressIndicator()
                    Text("Проверка авторизации...", modifier = Modifier.padding(top = 16.dp))
                }

                state.error != null -> {
                    println("❌ DEBUG ChatsScreen: Отображение ошибки: ${state.error}")
                    Text(
                        text = state.error ?: "Ошибка",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        println("🔄 DEBUG ChatsScreen: Повторная проверка авторизации")
                        viewModel.checkAuth()
                    }) {
                        Text("Повторить")
                    }
                }

                state.isLoggedIn -> {
                    println("✅ DEBUG ChatsScreen: Пользователь авторизован")
                    Text(
                        text = "Добро пожаловать в Eliza Chat!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Показываем ID пользователя из токенов
                    userId?.let {
                        Text("Ваш ID: $it")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text("Здесь будут ваши чаты")
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            println("➕ DEBUG ChatsScreen: Создание нового чата")
                            onNavigateToNewChat()
                        }
                    ) {
                        Text("Создать новый чат")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            println("🚪 DEBUG ChatsScreen: Выход через кнопку")
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Выйти")
                    }
                }

                else -> {
                    println("🚫 DEBUG ChatsScreen: Пользователь не авторизован")
                    Text(
                        text = "Вы не авторизованы",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Пожалуйста, войдите в систему")
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            println("🔙 DEBUG ChatsScreen: Переход к логину")
                            onLogout()
                        }
                    ) {
                        Text("Перейти к входу")
                    }
                }
            }
        }
    }
}