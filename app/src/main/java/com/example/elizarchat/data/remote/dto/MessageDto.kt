package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: Int,                    // ← Integer

    @SerialName("chat_id")
    val chatId: Int,               // ← Integer

    @SerialName("sender_id")       // ← ИЗМЕНЕНО: было user_id, стало sender_id (как на сервере)
    val senderId: Int,             // ← Integer

    @SerialName("content")
    val content: String,

    @SerialName("type")            // ← ИЗМЕНЕНО: было message_type, стало type (как на сервере)
    val type: String,              // "text", "image", "video", "file", "voice", "system"

    @SerialName("metadata")
    val metadata: String? = null,  // JSON строка

    @SerialName("reply_to")        // ← ДОБАВЛЕНО (есть на сервере)
    val replyTo: Int? = null,      // ← Integer

    @SerialName("status")          // ← ДОБАВЛЕНО (есть на сервере)
    val status: String? = null,    // "sending", "sent", "delivered", "read"

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO 8601 строка

    @SerialName("updated_at")
    val updatedAt: String? = null,  // ISO 8601 строка

    @SerialName("deleted_at")      // ← ДОБАВЛЕНО (есть на сервере)
    val deletedAt: String? = null,

    @SerialName("read_by")         // ← ДОБАВЛЕНО (есть на сервере)
    val readBy: List<Int>? = emptyList()  // ← List<Int>
)

// Ответы API
@Serializable
data class MessagesResponse(
    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),

    @SerialName("has_more")
    val hasMore: Boolean = false,

    @SerialName("total_count")
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

    @SerialName("reply_to")        // ← ДОБАВЛЕНО
    val replyTo: Int? = null
)

@Serializable
data class UpdateMessageRequest(
    @SerialName("content")
    val content: String
)

@Serializable
data class DeleteMessageRequest(
    @SerialName("message_id")
    val messageId: Int
)
