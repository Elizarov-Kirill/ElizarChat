package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: Int,                    // ← Integer

    @SerialName("chatId")  // ✅ ИСПРАВЛЕНО: camelCase
    val chatId: Int,               // ← Integer

    @SerialName("senderId")  // ✅ ИСПРАВЛЕНО: camelCase
    val senderId: Int,             // ← Integer

    @SerialName("content")
    val content: String,

    @SerialName("type")            // ← ИЗМЕНЕНО: было message_type, стало type (как на сервере)
    val type: String,              // "text", "image", "video", "file", "voice", "system"

    @SerialName("metadata")
    val metadata: String? = null,  // JSON строка

    @SerialName("replyTo")  // ✅ ИСПРАВЛЕНО: camelCase
    val replyTo: Int? = null,      // ← Integer

    @SerialName("status")          // ← ДОБАВЛЕНО (есть на сервере)
    val status: String? = null,    // "sending", "sent", "delivered", "read"

    @SerialName("createdAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val createdAt: String? = null,  // ISO 8601 строка

    @SerialName("updatedAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val updatedAt: String? = null,  // ISO 8601 строка

    @SerialName("deletedAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val deletedAt: String? = null,

    @SerialName("readBy")  // ✅ ИСПРАВЛЕНО: camelCase
    val readBy: List<Int>? = emptyList()  // ← List<Int>
)

// Ответы API
@Serializable
data class MessagesResponse(
    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),

    @SerialName("hasMore")  // ✅ ИСПРАВЛЕНО: camelCase
    val hasMore: Boolean = false,

    @SerialName("totalCount")  // ✅ ИСПРАВЛЕНО: camelCase
    val totalCount: Int = 0
)

@Serializable
data class MessageResponse(
    @SerialName("message")
    val message: MessageDto
)

// Запросы
@Serializable
data class SendMessageRequest(
    @SerialName("content")
    val content: String,

    @SerialName("type")            // ← ИЗМЕНЕНО: было message_type, стало type
    val type: String = "text",

    @SerialName("metadata")
    val metadata: String? = null,

    @SerialName("replyTo")  // ✅ ИСПРАВЛЕНО: camelCase
    val replyTo: Int? = null
)

@Serializable
data class UpdateMessageRequest(
    @SerialName("content")
    val content: String
)

@Serializable
data class DeleteMessageRequest(
    @SerialName("messageId")  // ✅ ИСПРАВЛЕНО: camelCase
    val messageId: Int
)