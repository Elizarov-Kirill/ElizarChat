// data/remote/dto/CreateChatResponse.kt
package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Ответ при создании чата
 * Сервер возвращает: {"success": true, "chat": {...}}
 */
@Serializable
data class CreateChatResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("chat")
    val chat: ChatDto? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("error")
    val error: String? = null
)