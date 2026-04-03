package com.example.elizarchat.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.websocket.WebSocketManager
import com.example.elizarchat.di.ServiceLocator
import com.example.elizarchat.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val tokenManager = remember { ServiceLocator.getTokenManager(context) }
    val apiManager = remember { ServiceLocator.getApiManager(context) }
    val webSocketManager = remember { ServiceLocator.getWebSocketManager(context) }

    val viewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    return AuthViewModel(apiManager, tokenManager, webSocketManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoginSuccessful, state.isLoading) {
        if (state.isLoginSuccessful && !state.isLoading) {
            viewModel.resetSuccessStates()
            onLoginSuccess()
        }
    }

    // Отслеживаем видимость клавиатуры
    val imeInsets = WindowInsets.ime
    val isImeVisible by remember {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isImeVisible) {
                    Arrangement.Top
                } else {
                    Arrangement.Center
                }
            ) {
                if (isImeVisible) {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(
                    text = "Вход",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // ✅ ИЗМЕНЕНО: поле для email ИЛИ username
                OutlinedTextField(
                    value = state.email,  // Переменная называется email, но хранит email или username
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email или имя пользователя") },  // ← ИЗМЕНЕН ТЕКСТ
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),  // ← ИЗМЕНЕНО: Text вместо Email
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле пароля (без изменений)
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка входа (без изменений)
                Button(
                    onClick = { viewModel.login() },
                    enabled = !state.isLoading &&
                            state.email.isNotBlank() &&  // emailOrUsername не пустое
                            state.password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Войти")
                    }
                }

                // Ошибка
                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ссылка на регистрацию
                TextButton(
                    onClick = onNavigateToRegister
                ) {
                    Text("Нет аккаунта? Зарегистрироваться")
                }

                if (isImeVisible) {
                    Spacer(modifier = Modifier.height(imeInsets.getBottom(density).dp))
                }
            }
        }
    }
}