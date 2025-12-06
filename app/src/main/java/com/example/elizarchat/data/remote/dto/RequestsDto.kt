package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    @SerialName("displayName")
    val displayName: String? = null,

    @SerialName("email")
    val email: String? = null
)

@Serializable
data class CreateChatRequest(
    @SerialName("type")
    val type: String, // "private", "group", "channel"

    @SerialName("participantIds")
    val participantIds: List<Long>,

    @SerialName("name")
    val name: String? = null
)

@Serializable
data class UpdateChatRequest(
    @SerialName("name")
    val name: String? = null
)

@Serializable
data class SendMessageRequest(
    @SerialName("chatId")
    val chatId: Long,

    @SerialName("content")
    val content: String,

    @SerialName("type")
    val type: String = "text"
)

@Serializable
data class UpdateMessageStatusRequest(
    @SerialName("status")
    val status: String // "sent", "delivered", "read"
)

@Serializable
data class MessagesResponse(
    @SerialName("messages")
    val messages: List<MessageDto>,

    @SerialName("hasMore")
    val hasMore: Boolean,

    @SerialName("total")
    val total: Int
)

@Serializable
data class FileUploadResponse(
    @SerialName("url")
    val url: String,

    @SerialName("filename")
    val filename: String,

    @SerialName("originalName")
    val originalName: String,

    @SerialName("size")
    val size: Long,

    @SerialName("mimeType")
    val mimeType: String
)