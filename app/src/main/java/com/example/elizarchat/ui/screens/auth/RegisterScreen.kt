package com.example.elizarchat.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.di.ServiceLocator
import com.example.elizarchat.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
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

    LaunchedEffect(state.isRegisterSuccessful, state.isLoading) {
        if (state.isRegisterSuccessful && !state.isLoading) {
            viewModel.resetSuccessStates()
            onRegisterSuccess()
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()), // Ограниченный скролл
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isImeVisible) {
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(
                    text = "Регистрация",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Поле username
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("Имя пользователя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле пароля
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле отображаемого имени
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    label = { Text("Отображаемое имя (опционально)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка регистрации
                Button(
                    onClick = { viewModel.register() },
                    enabled = !state.isLoading &&
                            state.username.isNotBlank() &&
                            state.email.isNotBlank() &&
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
                        Text("Зарегистрироваться")
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

                // Ссылка на вход
                TextButton(
                    onClick = onNavigateToLogin
                ) {
                    Text("Уже есть аккаунт? Войти")
                }

                // Отступ снизу
                Spacer(modifier = Modifier.height(if (isImeVisible) 32.dp else 64.dp))
            }
        }
    }
}