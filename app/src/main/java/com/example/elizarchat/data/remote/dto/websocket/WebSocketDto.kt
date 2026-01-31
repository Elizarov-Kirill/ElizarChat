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
        @SerialName("chat_id") val chatId: Int  // Изменено: Long → Int, snake_case
    ) : ClientMessage()

    @Serializable
    @SerialName("send_message")
    data class SendMessage(
        @SerialName("chat_id") val chatId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("content") val content: String,
        @SerialName("type") val type: String = "text",
        @SerialName("reply_to") val replyTo: Int? = null,  // Добавлено
        @SerialName("metadata") val metadata: String? = null  // Добавлено
    ) : ClientMessage()

    @Serializable
    @SerialName("mark_read")
    data class MarkRead(
        @SerialName("chat_id") val chatId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("message_ids") val messageIds: List<Int>  // Изменено: Long → Int, snake_case
    ) : ClientMessage()

    @Serializable
    @SerialName("typing")
    data class Typing(
        @SerialName("chat_id") val chatId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("is_typing") val isTyping: Boolean = true  // Добавлено
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
    @SerialName("message_updated")
    data class MessageUpdated(
        @SerialName("message") val message: MessageDto,
        @SerialName("update_type") val updateType: String  // "content", "status", "read_by", "deleted"
    ) : ServerMessage()  // Добавлено

    @Serializable
    @SerialName("message_status")
    data class MessageStatus(
        @SerialName("message_id") val messageId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("status") val status: String,      // "sending", "sent", "delivered", "read"
        @SerialName("updated_at") val updatedAt: String  // snake_case
    ) : ServerMessage()

    @Serializable
    @SerialName("user_status")
    data class UserStatus(
        @SerialName("user_id") val userId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("is_online") val isOnline: Boolean,  // snake_case
        @SerialName("last_seen") val lastSeen: String?,  // snake_case
        @SerialName("status") val status: String? = null  // "online", "offline", "busy", "away"
    ) : ServerMessage()

    @Serializable
    @SerialName("new_chat")
    data class NewChat(
        @SerialName("chat") val chat: ChatDto
    ) : ServerMessage()

    @Serializable
    @SerialName("chat_updated")
    data class ChatUpdated(
        @SerialName("chat") val chat: ChatDto,
        @SerialName("update_type") val updateType: String  // "name", "avatar", "members", "last_message"
    ) : ServerMessage()  // Добавлено

    @Serializable
    @SerialName("user_typing")
    data class UserTyping(
        @SerialName("chat_id") val chatId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("user_id") val userId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("is_typing") val isTyping: Boolean  // Добавлено
    ) : ServerMessage()

    @Serializable
    @SerialName("notification")
    data class Notification(
        @SerialName("title") val title: String,
        @SerialName("body") val body: String,
        @SerialName("chat_id") val chatId: Int,  // Изменено: Long → Int, snake_case
        @SerialName("message_id") val messageId: Int? = null  // Изменено: Long → Int, snake_case
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

    @Serializable
    @SerialName("auth_required")
    object AuthRequired : ServerMessage()  // Добавлено

    @Serializable
    @SerialName("auth_success")
    data class AuthSuccess(
        @SerialName("user_id") val userId: Int  // Изменено: Long → Int, snake_case
    ) : ServerMessage()  // Добавлено
}