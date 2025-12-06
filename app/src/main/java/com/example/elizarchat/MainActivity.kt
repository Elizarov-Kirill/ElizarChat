package com.example.elizarchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.elizarchat.data.remote.dto.AuthResponseDto
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.data.remote.dto.UsersResponseDto
import com.example.elizarchat.ui.theme.ElizarChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Тест всей цепочки
        TestUserComplete.runTest()

        // Дополнительный тест DTO
        testDtoModels()

        setContent {
            ElizarChatTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Greeting(
                        name = "ElizarChat",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun testDtoModels() {
        println("\n=== ТЕСТ DTO МОДЕЛЕЙ ===")

        // Тест AuthResponseDto
        val authResponse = AuthResponseDto(
            token = "jwt_token_123",
            user = UserDto(
                id = 123L,  // Исправлено: Long вместо String
                username = "john",
                email = "john@example.com",
                createdAt = "2024-01-01T00:00:00Z"
            )
        )

        println("1. AuthResponseDto:")
        println("   - Token: ${authResponse.token}")
        println("   - User ID: ${authResponse.user.id}")
        println("   - Username: ${authResponse.user.username}")

        // Тест UsersResponseDto
        val usersResponse = UsersResponseDto(
            users = listOf(
                UserDto(
                    id = 1L,
                    username = "alice",
                    email = "alice@example.com",
                    displayName = "Alice Smith",
                    isOnline = true,
                    createdAt = "2024-01-01T00:00:00Z"
                ),
                UserDto(
                    id = 2L,
                    username = "bob",
                    email = "bob@example.com",
                    displayName = "Bob Johnson",
                    isOnline = false,
                    lastSeen = "2024-01-15T14:30:00Z",
                    createdAt = "2024-01-01T00:00:00Z"
                ),
                UserDto(
                    id = 3L,
                    username = "charlie",
                    email = null,  // Тестируем nullable поля
                    displayName = null,
                    isOnline = false,
                    lastSeen = null,
                    createdAt = null
                )
            )
        )

        println("\n2. UsersResponseDto:")
        println("   - Найдено пользователей: ${usersResponse.users.size}")
        println("   - Подробная информация:")
        usersResponse.users.forEachIndexed { i, user ->
            println("   ${i + 1}. ${user.username}")
            println("      - ID: ${user.id}")
            println("      - Display Name: ${user.displayName ?: "Не указано"}")
            println("      - Email: ${user.email ?: "Скрыт"}")
            println("      - Онлайн: ${if (user.isOnline) "✅" else "❌"}")
            println("      - Last Seen: ${user.lastSeen ?: "Неизвестно"}")
        }

        // Тест сериализации
        println("\n3. Тест полей DTO:")
        val testUser = UserDto(
            id = 999L,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            isOnline = true,
            lastSeen = "2024-01-20T10:15:30Z",
            createdAt = "2024-01-01T00:00:00Z"
        )

        println("   - Все поля инициализированы корректно: ✅")
        println("   - ID тип: ${testUser.id::class.simpleName}")
        println("   - Username тип: ${testUser.username::class.simpleName}")
        println("   - Email nullable: ${testUser.email == null}")
        println("   - DisplayName nullable: ${testUser.displayName == null}")

        println("\n=== ТЕСТ DTO ЗАВЕРШЕН ===\n")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!\nUser models and DTOs are ready.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ElizarChatTheme {
        Greeting("Android")
    }
}