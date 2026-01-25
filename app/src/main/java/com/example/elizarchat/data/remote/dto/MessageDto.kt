package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: String,

    @SerialName("chat_id")
    val chatId: String,

    @SerialName("user_id")
    val userId: String,  // соответствует senderId в domain

    @SerialName("content")
    val content: String,

    @SerialName("message_type")
    val messageType: String,  // "text", "image", "video", "file", "voice", "system"

    @SerialName("metadata")
    val metadata: String? = null,  // JSON строка

    @SerialName("is_edited")
    val isEdited: Boolean = false,

    @SerialName("is_deleted")
    val isDeleted: Boolean = false,

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO 8601 строка

    @SerialName("updated_at")
    val updatedAt: String? = null  // ISO 8601 строка
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

    @SerialName("message_type")
    val messageType: String = "text",

    @SerialName("metadata")
    val metadata: String? = null
)

@Serializable
data class UpdateMessageRequest(
    @SerialName("content")
    val content: String
)

@Serializable
data class DeleteMessageRequest(
    @SerialName("message_id")
    val messageId: String
)