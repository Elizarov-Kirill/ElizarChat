package com.example.elizarchat.data.remote.dto.websocket

import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.MessageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Сообщения от клиента к серверу
@Serializable
sealed class ClientMessage {
    @Serializable
    @SerialName("ping")
    object Ping : ClientMessage()

    @Serializable
    @SerialName("subscribe")
    data class Subscribe(
        @SerialName("chatId") val chatId: Long
    ) : ClientMessage()

    @Serializable
    @SerialName("send_message")
    data class SendMessage(
        @SerialName("chatId") val chatId: Long,
        @SerialName("content") val content: String,
        @SerialName("type") val type: String = "text"
    ) : ClientMessage()

    @Serializable
    @SerialName("mark_read")
    data class MarkRead(
        @SerialName("chatId") val chatId: Long,
        @SerialName("messageIds") val messageIds: List<Long>
    ) : ClientMessage()
}

// Сообщения от сервера к клиенту
@Serializable
sealed class ServerMessage {
    @Serializable
    @SerialName("new_message")
    data class NewMessage(
        @SerialName("message") val message: MessageDto
    ) : ServerMessage()

    @Serializable
    @SerialName("message_status")
    data class MessageStatus(
        @SerialName("messageId") val messageId: Long,
        @SerialName("status") val status: String,
        @SerialName("updatedAt") val updatedAt: String
    ) : ServerMessage()

    @Serializable
    @SerialName("user_status")
    data class UserStatus(
        @SerialName("userId") val userId: Long,
        @SerialName("isOnline") val isOnline: Boolean,
        @SerialName("lastSeen") val lastSeen: String?
    ) : ServerMessage()

    @Serializable
    @SerialName("new_chat")
    data class NewChat(
        @SerialName("chat") val chat: ChatDto
    ) : ServerMessage()

    @Serializable
    @SerialName("notification")
    data class Notification(
        @SerialName("title") val title: String,
        @SerialName("body") val body: String,
        @SerialName("chatId") val chatId: Long,
        @SerialName("messageId") val messageId: Long
    ) : ServerMessage()

    @Serializable
    @SerialName("error")
    data class Error(
        @SerialName("code") val code: String,
        @SerialName("message") val message: String,
        @SerialName("details") val details: Map<String, String> = emptyMap()
    ) : ServerMessage()

    @Serializable
    @SerialName("pong")
    data class Pong(
        @SerialName("timestamp") val timestamp: String
    ) : ServerMessage()
}