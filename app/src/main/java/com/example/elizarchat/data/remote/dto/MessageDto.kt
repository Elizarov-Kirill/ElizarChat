package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id")
    val id: Long,

    @SerialName("chatId")
    val chatId: Long,

    @SerialName("senderId")
    val senderId: Long,

    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String, // "text", "image", "video", etc.

    @SerialName("status")
    val status: String = "sent", // "sending", "sent", "delivered", "read"

    @SerialName("createdAt")
    val createdAt: String, // ISO строка

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("readBy")
    val readBy: List<Long> = emptyList(),

    @SerialName("attachments")
    val attachments: List<AttachmentDto>? = null,

    @SerialName("replyTo")
    val replyTo: Long? = null
)

@Serializable
data class AttachmentDto(
    @SerialName("id")
    val id: String,

    @SerialName("url")
    val url: String,

    @SerialName("type")
    val type: String, // "image", "video", "audio", "file"

    @SerialName("name")
    val name: String? = null,

    @SerialName("size")
    val size: Long? = null,

    @SerialName("duration")
    val duration: Long? = null,

    @SerialName("thumbnailUrl")
    val thumbnailUrl: String? = null
)

@Serializable
data class MessagesResponseDto(
    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),

    @SerialName("hasMore")
    val hasMore: Boolean = false,

    @SerialName("total")
    val total: Int = 0
)

@Serializable
data class SendMessageRequestDto(
    @SerialName("chatId")
    val chatId: Long,

    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String = "text",

    @SerialName("replyTo")
    val replyTo: Long? = null,

    @SerialName("attachments")
    val attachments: List<AttachmentDto>? = null
)

@Serializable
data class SendMessageResponseDto(
    @SerialName("message")
    val message: MessageDto,

    @SerialName("chat")
    val chat: ChatDto? = null
)

@Serializable
data class UpdateMessageStatusRequestDto(
    @SerialName("status")
    val status: String // "delivered", "read"
)