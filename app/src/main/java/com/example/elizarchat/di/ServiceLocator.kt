// 📁 di/ServiceLocator.kt
package com.example.elizarchat.di

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.websocket.WebSocketManager

object ServiceLocator {
    @Volatile
    private var _webSocketManager: WebSocketManager? = null

    @Volatile
    private var _apiManager: ApiManager? = null

    @Volatile
    private var _tokenManager: TokenManager? = null

    /**
     * Получить WebSocketManager (синглтон)
     */
    fun getWebSocketManager(context: Context): WebSocketManager {
        return _webSocketManager ?: synchronized(this) {
            _webSocketManager ?: WebSocketManager(
                context = context.applicationContext,
                tokenManager = getTokenManager(context),
                apiManager = getApiManager(context)
            ).also {
                _webSocketManager = it
            }
        }
    }

    /**
     * Получить ApiManager (синглтон с поддержкой refresh токена)
     */
    fun getApiManager(context: Context): ApiManager {
        return _apiManager ?: synchronized(this) {
            _apiManager ?: ApiManager.getInstance(
                context.applicationContext,
                getTokenManager(context)
            ).also {
                _apiManager = it
            }
        }
    }

    /**
     * Получить неавторизованный ApiManager (только для authApi)
     */
    fun getUnauthenticatedApiManager(context: Context): ApiManager {
        return ApiManager.getUnauthenticatedInstance(context.applicationContext)
    }

    /**
     * Получить TokenManager (синглтон)
     */
    fun getTokenManager(context: Context): TokenManager {
        return _tokenManager ?: synchronized(this) {
            _tokenManager ?: TokenManager.getInstance(context.applicationContext).also {
                _tokenManager = it
            }
        }
    }

    /**
     * Очистить все синглтоны (при логауте или завершении приложения)
     */
    fun clear() {
        synchronized(this) {
            _webSocketManager?.disconnect()
            _webSocketManager = null
            _apiManager = null
            _tokenManager = null
        }
    }

    /**
     * Проверить, инициализированы ли зависимости
     */
    fun isInitialized(): Boolean {
        return _tokenManager != null && _apiManager != null
    }
}