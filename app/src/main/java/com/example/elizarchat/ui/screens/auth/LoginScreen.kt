package com.example.elizarchat.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.getElizarChatApplication
import com.example.elizarchat.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    println("DEBUG: LoginScreen создается")

    val apiManager = remember {
        println("DEBUG: Создание ApiManager в LoginScreen")
        ApiManager(context)
    }

    val tokenManager = remember {
        println("DEBUG: Получение TokenManager в LoginScreen")
        TokenManager.getInstance(context)
    }

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(apiManager, tokenManager)
    )

    val state by viewModel.state.collectAsState()

    println("DEBUG: LoginScreen состояние: isLoginSuccessful=${state.isLoginSuccessful}, isLoading=${state.isLoading}")

    // Реакция на успешный логин
    LaunchedEffect(state.isLoginSuccessful, state.isLoading) {
        println("DEBUG: LaunchedEffect сработал, isLoginSuccessful=${state.isLoginSuccessful}, isLoading=${state.isLoading}")

        if (state.isLoginSuccessful && !state.isLoading) {
            println("DEBUG: Условие навигации выполнено! Переход на chats...")
            // Небольшая задержка для стабильности
            delay(100)
            viewModel.resetSuccessStates()
            println("DEBUG: Состояние сброшено, вызываем onLoginSuccess")
            onLoginSuccess()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Eliza Chat",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Поле email
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка входа
            Button(
                onClick = { viewModel.login() },
                enabled = !state.isLoading &&
                        state.email.isNotBlank() &&
                        state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
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
                Text("Нет аккаунта? Зарегистрируйтесь")
            }
        }
    }
}