package com.example.elizarchat.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.elizarchat.ui.screens.auth.LoginScreen
import com.example.elizarchat.ui.screens.auth.RegisterScreen
import com.example.elizarchat.ui.screens.main.ChatsScreen

@Composable
fun ElizarNavigation() {
    val navController = rememberNavController()

    println("DEBUG: Начало навигации, startDestination = login")

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            println("DEBUG: Отображение экрана login")
            LoginScreen(
                onNavigateToRegister = {
                    println("DEBUG: Навигация на register")
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    println("DEBUG: onLoginSuccess вызван, переход на chats")
                    navController.navigate("chats") {
                        popUpTo("login") { inclusive = true }
                        println("DEBUG: Навигация выполнена с очисткой стека")
                    }
                }
            )
        }

        composable("register") {
            println("DEBUG: Отображение экрана register")
            RegisterScreen(
                onNavigateToLogin = {
                    println("DEBUG: Возврат на login")
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    println("DEBUG: onRegisterSuccess вызван, переход на chats")
                    navController.navigate("chats") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("chats") {
            println("📍 DEBUG Navigation: Переход на экран chats")
            ChatsScreen(
                onNavigateToChat = { chatId ->
                    println("💬 DEBUG Navigation: Переход к чату $chatId")
                    // navController.navigate("chat/$chatId")
                },
                onNavigateToNewChat = {
                    println("➕ DEBUG Navigation: Создание нового чата")
                    // navController.navigate("newChat")
                },
                onLogout = {
                    println("🚪 DEBUG Navigation: Выход из чатов")
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}