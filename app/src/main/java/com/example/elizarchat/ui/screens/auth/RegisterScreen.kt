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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.getElizarChatApplication
import com.example.elizarchat.ui.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val application = getElizarChatApplication()

    println("🚀 DEBUG RegisterScreen: Composable создается")
    println("🚀 DEBUG RegisterScreen: application = $application")
    println("🚀 DEBUG RegisterScreen: apiManager = ${application.apiManager}")
    println("🚀 DEBUG RegisterScreen: tokenManager = ${application.tokenManager}")

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(
            apiManager = application.apiManager,
            tokenManager = application.tokenManager
        )
    )

    println("🚀 DEBUG RegisterScreen: ViewModel создан")

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Выводим текущее состояние
    println("📊 DEBUG RegisterScreen: Текущее состояние - " +
            "isRegisterSuccessful=${state.isRegisterSuccessful}, " +
            "isLoading=${state.isLoading}, " +
            "error=${state.error}, " +
            "username=${state.username}, " +
            "email=${state.email}")

    // Реакция на успешную регистрацию
    LaunchedEffect(state.isRegisterSuccessful, state.isLoading) {
        println("🔄 DEBUG RegisterScreen: LaunchedEffect сработал с ключами: " +
                "isRegisterSuccessful=${state.isRegisterSuccessful}, " +
                "isLoading=${state.isLoading}")

        if (state.isRegisterSuccessful && !state.isLoading) {
            println("✅ DEBUG RegisterScreen: УСПЕХ! Условие навигации выполнено")
            println("🔄 DEBUG RegisterScreen: Сбрасываем состояния...")

            // Сбрасываем состояние перед навигацией
            viewModel.resetSuccessStates()

            println("🔄 DEBUG RegisterScreen: Вызываем onRegisterSuccess()...")
            onRegisterSuccess()

            println("✅ DEBUG RegisterScreen: onRegisterSuccess() вызван")
        } else if (state.isRegisterSuccessful && state.isLoading) {
            println("⚠️ DEBUG RegisterScreen: isRegisterSuccessful=true, но isLoading=true - ждем...")
        } else {
            println("⏸️ DEBUG RegisterScreen: Условие навигации НЕ выполнено")
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Поле username
            OutlinedTextField(
                value = state.username,
                onValueChange = { newValue ->
                    println("📝 DEBUG RegisterScreen: Обновление username: '$newValue'")
                    viewModel.updateUsername(newValue)
                },
                label = { Text("Имя пользователя") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле email
            OutlinedTextField(
                value = state.email,
                onValueChange = { newValue ->
                    println("📝 DEBUG RegisterScreen: Обновление email: '$newValue'")
                    viewModel.updateEmail(newValue)
                },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            OutlinedTextField(
                value = state.password,
                onValueChange = { newValue ->
                    println("📝 DEBUG RegisterScreen: Обновление password: '${"*".repeat(newValue.length)}'")
                    viewModel.updatePassword(newValue)
                },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле отображаемого имени (опционально)
            OutlinedTextField(
                value = state.displayName,
                onValueChange = { newValue ->
                    println("📝 DEBUG RegisterScreen: Обновление displayName: '$newValue'")
                    viewModel.updateDisplayName(newValue)
                },
                label = { Text("Отображаемое имя (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка регистрации
            Button(
                onClick = {
                    println("🔼 DEBUG RegisterScreen: Нажата кнопка регистрации")
                    println("📋 DEBUG RegisterScreen: Данные для отправки - " +
                            "username='${state.username}', " +
                            "email='${state.email}', " +
                            "passwordLength=${state.password.length}, " +
                            "displayName='${state.displayName}'")
                    viewModel.register()
                },
                enabled = !state.isLoading &&
                        state.username.isNotBlank() &&
                        state.email.isNotBlank() &&
                        state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    println("⏳ DEBUG RegisterScreen: Отображение индикатора загрузки")
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Зарегистрироваться")
                }
            }

            // Ошибка
            state.error?.let { error ->
                println("❌ DEBUG RegisterScreen: Отображение ошибки: '$error'")
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
                onClick = {
                    println("🔙 DEBUG RegisterScreen: Нажата кнопка 'Уже есть аккаунт?'")
                    onNavigateToLogin()
                }
            ) {
                Text("Уже есть аккаунт? Войти")
            }
        }
    }
}