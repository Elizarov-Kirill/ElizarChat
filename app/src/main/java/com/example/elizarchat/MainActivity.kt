package com.example.elizarchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.elizarchat.data.remote.service.ServerTestService
import com.example.elizarchat.ui.theme.ElizarChatTheme

class MainActivity : ComponentActivity() {
    private lateinit var serverTestService: ServerTestService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        println("\n" + "=".repeat(50))
        println("🚀 ELIZARCHAT CLIENT STARTING")
        println("=".repeat(50))

        // Запускаем тесты моделей
        TestUserComplete.runTest()
        TestChatComplete.runTest()
        TestMessageComplete.runTest()

        // Тестируем подключение к реальному серверу
        serverTestService = ServerTestService()
        serverTestService.runFullConnectionTest()

        setContent {
            ElizarChatTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Greeting(
                        name = "ElizarChat",
                        modifier = Modifier.padding(innerPadding),
                        serverTestService = serverTestService
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    serverTestService: ServerTestService
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ElizarChat Client",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Подключение к серверу...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Статус подключения
        var connectionStatus by remember { mutableStateOf("Проверка соединения...") }

        LaunchedEffect(Unit) {
            // Обновляем статус
            delay(2000)
            connectionStatus = "✅ Модели данных протестированы"

            delay(2000)
            connectionStatus = "🔧 Настройка API соединения..."

            delay(2000)
            connectionStatus = "📡 Подключение к stalinvdote.ru"
        }

        Text(
            text = connectionStatus,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка для ручного тестирования
        Button(
            onClick = {
                serverTestService.runFullConnectionTest()
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Тестировать подключение")
        }
    }
}