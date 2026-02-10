// 📁 di/ServiceLocator.kt
package com.example.elizarchat.di

import android.content.Context
import com.example.elizarchat.data.local.session.TokenManager
import com.example.elizarchat.data.remote.ApiManager
import com.example.elizarchat.data.remote.websocket.WebSocketManager

object ServiceLocator {
    private var _webSocketManager: WebSocketManager? = null
    private var _apiManager: ApiManager? = null
    private var _tokenManager: TokenManager? = null

    fun getWebSocketManager(context: Context): WebSocketManager {
        return _webSocketManager ?: synchronized(this) {
            _webSocketManager ?: WebSocketManager(
                context = context,
                tokenManager = getTokenManager(context),
                apiManager = getApiManager(context) // Передаем ApiManager
            ).also {
                _webSocketManager = it
            }
        }
    }

    fun getApiManager(context: Context): ApiManager {
        return _apiManager ?: synchronized(this) {
            _apiManager ?: ApiManager(context).also {
                _apiManager = it
            }
        }
    }

    fun getTokenManager(context: Context): TokenManager {
        return _tokenManager ?: synchronized(this) {
            _tokenManager ?: TokenManager.getInstance(context).also {
                _tokenManager = it
            }
        }
    }

    fun clear() {
        _webSocketManager?.disconnect()
        _webSocketManager = null
        _apiManager = null
        _tokenManager = null
    }
}