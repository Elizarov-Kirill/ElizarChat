package com.example.elizarchat.data.remote.websocket

sealed class WebSocketState {
    object Disconnected : WebSocketState()
    object Connecting : WebSocketState()
    data class Connected(val connectionId: String? = null) : WebSocketState()
    data class Error(val message: String) : WebSocketState()
}