// 📁 MainActivity.kt
package com.example.elizarchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.elizarchat.di.ServiceLocator
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.ui.screens.ElizarNavigation
import com.example.elizarchat.ui.theme.ElizarChatTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var webSocketManager: com.example.elizarchat.data.remote.websocket.WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация зависимостей
        tokenManager = ServiceLocator.getTokenManager(this)
        webSocketManager = ServiceLocator.getWebSocketManager(this)

        // Регистрируем WebSocketManager как LifecycleObserver (новый API)
        lifecycle.addObserver(webSocketManager)

        setContent {
            ElizarChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ElizarNavigation()
                }
            }
        }

        // Автоматический запуск WebSocket если пользователь авторизован
        lifecycleScope.launch {
            if (tokenManager.isLoggedIn()) {
                println("🚀 MainActivity: Пользователь авторизован, запускаем WebSocket")
                webSocketManager.connect()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(webSocketManager)
        // Очистка при полном уничтожении активности
        if (isFinishing) {
            ServiceLocator.clear()
        }
    }
}

// test12345@test11.com
// edcrfv