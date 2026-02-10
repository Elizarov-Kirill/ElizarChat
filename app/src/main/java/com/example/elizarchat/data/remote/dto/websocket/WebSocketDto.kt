// 📁 data/remote/dto/websocket/WebSocketDto.kt
package com.example.elizarchat.data.remote.dto.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ========================
// ВХОДЯЩИЕ сообщения (от клиента к серверу)
// ========================

@Serializable
sealed class WebSocketIncomingMessage {
    @SerialName("type") abstract val type: String
}

// Ping сообщение (keep-alive)
@Serializable
@SerialName("ping")
data class PingMessage(
    @SerialName("type") override val type: String = "ping"
) : WebSocketIncomingMessage()

// Сообщение печатания
@Serializable
@SerialName("typing")
data class TypingMessage(
    @SerialName("type") override val type: String = "typing",
    @SerialName("chatId") val chatId: Int,
    @SerialName("isTyping") val isTyping: Boolean
) : WebSocketIncomingMessage()

// Отправка сообщения
@Serializable
@SerialName("message")
data class SendMessageRequest(
    @SerialName("type") override val type: String = "message",
    @SerialName("chatId") val chatId: Int,
    @SerialName("content") val content: String,
    @SerialName("messageType") val messageType: String = "text",
    @SerialName("replyTo") val replyTo: Int? = null,
    @SerialName("metadata") val metadata: String? = null // JSON строка
) : WebSocketIncomingMessage()

// Подписка на чат
@Serializable
@SerialName("subscribe")
data class SubscribeChatMessage(
    @SerialName("type") override val type: String = "subscribe",
    @SerialName("chatId") val chatId: Int
) : WebSocketIncomingMessage()

// Отписка от чата
@Serializable
@SerialName("unsubscribe")
data class UnsubscribeChatMessage(
    @SerialName("type") override val type: String = "unsubscribe",
    @SerialName("chatId") val chatId: Int
) : WebSocketIncomingMessage()

// Подтверждение прочтения
@Serializable
@SerialName("read_receipt")
data class ReadReceiptMessage(
    @SerialName("type") override val type: String = "read_receipt",
    @SerialName("chatId") val chatId: Int,
    @SerialName("messageIds") val messageIds: List<Int>
) : WebSocketIncomingMessage()

// ========================
// ИСХОДЯЩИЕ сообщения (от сервера к клиенту)
// ========================

@Serializable
sealed class WebSocketOutgoingMessage {
    @SerialName("type") abstract val type: String
}

// Welcome сообщение (при подключении)
@Serializable
@SerialName("welcome")
data class WelcomeMessage(
    @SerialName("type") override val type: String = "welcome",
    @SerialName("message") val message: String,
    @SerialName("user") val user: WelcomeUser? = null,
    @SerialName("chats") val chats: List<WelcomeChat> = emptyList(),
    @SerialName("serverInfo") val serverInfo: ServerInfo? = null
) : WebSocketOutgoingMessage()

@Serializable
data class WelcomeUser(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("displayName") val displayName: String? = null
)

@Serializable
data class WelcomeChat(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("unreadCount") val unreadCount: Int = 0,
    @SerialName("lastMessageAt") val lastMessageAt: String? = null
)

@Serializable
data class ServerInfo(
    @SerialName("version") val version: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("connectionId") val connectionId: String
)

// Новое сообщение
@Serializable
@SerialName("new_message")
data class NewMessageEvent(
    @SerialName("type") override val type: String = "new_message",
    @SerialName("chatId") val chatId: Int,
    @SerialName("message") val message: ChatMessage
) : WebSocketOutgoingMessage()

// Сообщение о печатании
@Serializable
@SerialName("user_typing")
data class UserTypingEvent(
    @SerialName("type") override val type: String = "user_typing",
    @SerialName("chatId") val chatId: Int,
    @SerialName("userId") val userId: Int,
    @SerialName("isTyping") val isTyping: Boolean
) : WebSocketOutgoingMessage()

// Подтверждение отправки сообщения
@Serializable
@SerialName("message_sent")
data class MessageSentConfirmation(
    @SerialName("type") override val type: String = "message_sent",
    @SerialName("tempId") val tempId: String? = null,
    @SerialName("messageId") val messageId: Int,
    @SerialName("chatId") val chatId: Int,
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение прочтения
@Serializable
@SerialName("read_receipt_ack")
data class ReadReceiptAck(
    @SerialName("type") override val type: String = "read_receipt_ack",
    @SerialName("chatId") val chatId: Int,
    @SerialName("messageIds") val messageIds: List<Int>,
    @SerialName("userId") val userId: Int,
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Pong (ответ на ping)
@Serializable
@SerialName("pong")
data class PongMessage(
    @SerialName("type") override val type: String = "pong",
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Ошибка
@Serializable
@SerialName("error")
data class ErrorMessage(
    @SerialName("type") override val type: String = "error",
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String,
    @SerialName("details") val details: Map<String, String>? = null
) : WebSocketOutgoingMessage()

// Системное уведомление
@Serializable
@SerialName("system")
data class SystemMessage(
    @SerialName("type") override val type: String = "system",
    @SerialName("message") val message: String,
    @SerialName("data") val data: Map<String, String>? = null
) : WebSocketOutgoingMessage()

// Обновление статуса пользователя
@Serializable
@SerialName("user_status")
data class UserStatusUpdate(
    @SerialName("type") override val type: String = "user_status",
    @SerialName("userId") val userId: Int,
    @SerialName("isOnline") val isOnline: Boolean,
    @SerialName("status") val status: String? = null,
    @SerialName("lastSeen") val lastSeen: String? = null
) : WebSocketOutgoingMessage()

// Обновление чата (новый участник, изменение названия и т.д.)
@Serializable
@SerialName("chat_update")
data class ChatUpdate(
    @SerialName("type") override val type: String = "chat_update",
    @SerialName("chatId") val chatId: Int,
    @SerialName("action") val action: String, // "member_added", "member_removed", "name_changed", "description_changed"
    @SerialName("data") val data: Map<String, String>? = null,
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// ========================
// ОБЩИЕ МОДЕЛИ
// ========================

@Serializable
data class ChatMessage(
    @SerialName("id") val id: Int,
    @SerialName("content") val content: String,
    @SerialName("senderId") val senderId: Int,
    @SerialName("chatId") val chatId: Int,
    @SerialName("type") val type: String = "text",
    @SerialName("status") val status: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("metadata") val metadata: Map<String, String>? = null,
    @SerialName("replyTo") val replyTo: Int? = null
)

// ========================
// СОСТОЯНИЕ WEBSOCKET
// ========================

@Serializable
sealed class WebSocketState {
    @SerialName("type") abstract val type: String

    @Serializable
    @SerialName("disconnected")
    object Disconnected : WebSocketState() {
        override val type: String = "disconnected"
    }

    @Serializable
    @SerialName("connecting")
    object Connecting : WebSocketState() {
        override val type: String = "connecting"
    }

    @Serializable
    @SerialName("connected")
    data class Connected(
        @SerialName("type") override val type: String = "connected",
        @SerialName("connectionId") val connectionId: String? = null
    ) : WebSocketState()

    @Serializable
    @SerialName("error")
    data class Error(
        @SerialName("type") override val type: String = "error",
        @SerialName("message") val message: String
    ) : WebSocketState()
}

// ========================
// УТИЛИТЫ ДЛЯ РАБОТЫ С WEBSOCKET
// ========================

object WebSocketMessageHelper {

    // Функция для определения типа сообщения
    fun getMessageType(jsonString: String): String? {
        return try {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val jsonElement = json.parseToJsonElement(jsonString)
            if (jsonElement is kotlinx.serialization.json.JsonObject) {
                jsonElement["type"]?.toString()?.trim('"')
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Создание метаданных в формате JSON строки
    fun createMetadata(vararg pairs: Pair<String, String>): String? {
        return if (pairs.isEmpty()) {
            "{}"
        } else {
            val json = kotlinx.serialization.json.Json { encodeDefaults = true }
            json.encodeToString(pairs.toMap())
        }
    }

    // Десериализация входящего сообщения
    fun deserializeIncomingMessage(jsonString: String): WebSocketIncomingMessage? {
        return try {
            val type = getMessageType(jsonString)
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

            when (type) {
                "ping" -> json.decodeFromString(PingMessage.serializer(), jsonString)
                "typing" -> json.decodeFromString(TypingMessage.serializer(), jsonString)
                "message" -> json.decodeFromString(SendMessageRequest.serializer(), jsonString)
                "subscribe" -> json.decodeFromString(SubscribeChatMessage.serializer(), jsonString)
                "unsubscribe" -> json.decodeFromString(UnsubscribeChatMessage.serializer(), jsonString)
                "read_receipt" -> json.decodeFromString(ReadReceiptMessage.serializer(), jsonString)
                else -> null
            }
        } catch (e: Exception) {
            println("❌ Ошибка десериализации входящего сообщения: ${e.message}")
            null
        }
    }

    // Десериализация исходящего сообщения
    fun deserializeOutgoingMessage(jsonString: String): WebSocketOutgoingMessage? {
        return try {
            val type = getMessageType(jsonString)
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

            when (type) {
                "welcome" -> json.decodeFromString(WelcomeMessage.serializer(), jsonString)
                "new_message" -> json.decodeFromString(NewMessageEvent.serializer(), jsonString)
                "user_typing" -> json.decodeFromString(UserTypingEvent.serializer(), jsonString)
                "message_sent" -> json.decodeFromString(MessageSentConfirmation.serializer(), jsonString)
                "read_receipt_ack" -> json.decodeFromString(ReadReceiptAck.serializer(), jsonString)
                "pong" -> json.decodeFromString(PongMessage.serializer(), jsonString)
                "error" -> json.decodeFromString(ErrorMessage.serializer(), jsonString)
                "system" -> json.decodeFromString(SystemMessage.serializer(), jsonString)
                "user_status" -> json.decodeFromString(UserStatusUpdate.serializer(), jsonString)
                "chat_update" -> json.decodeFromString(ChatUpdate.serializer(), jsonString)
                else -> null
            }
        } catch (e: Exception) {
            println("❌ Ошибка десериализации исходящего сообщения: ${e.message}")
            null
        }
    }

    // Сериализация входящего сообщения
    fun serializeIncomingMessage(message: WebSocketIncomingMessage): String {
        return try {
            val json = kotlinx.serialization.json.Json {
                encodeDefaults = true
                classDiscriminator = "type"
            }

            when (message) {
                is PingMessage -> json.encodeToString(message)
                is TypingMessage -> json.encodeToString(message)
                is SendMessageRequest -> json.encodeToString(message)
                is SubscribeChatMessage -> json.encodeToString(message)
                is UnsubscribeChatMessage -> json.encodeToString(message)
                is ReadReceiptMessage -> json.encodeToString(message)
            }
        } catch (e: Exception) {
            println("❌ Ошибка сериализации входящего сообщения: ${e.message}")
            "{}"
        }
    }
}

// ========================
// КОНСТАНТЫ ДЛЯ WEBSOCKET
// ========================

object WebSocketConstants {
    // Типы сообщений
    const val TYPE_PING = "ping"
    const val TYPE_TYPING = "typing"
    const val TYPE_MESSAGE = "message"
    const val TYPE_SUBSCRIBE = "subscribe"
    const val TYPE_UNSUBSCRIBE = "unsubscribe"
    const val TYPE_READ_RECEIPT = "read_receipt"

    const val TYPE_WELCOME = "welcome"
    const val TYPE_NEW_MESSAGE = "new_message"
    const val TYPE_USER_TYPING = "user_typing"
    const val TYPE_MESSAGE_SENT = "message_sent"
    const val TYPE_READ_RECEIPT_ACK = "read_receipt_ack"
    const val TYPE_PONG = "pong"
    const val TYPE_ERROR = "error"
    const val TYPE_SYSTEM = "system"
    const val TYPE_USER_STATUS = "user_status"
    const val TYPE_CHAT_UPDATE = "chat_update"

    // Статусы сообщений
    const val STATUS_SENDING = "sending"
    const val STATUS_SENT = "sent"
    const val STATUS_DELIVERED = "delivered"
    const val STATUS_READ = "read"
    const val STATUS_ERROR = "error"

    // Типы сообщений
    const val MESSAGE_TYPE_TEXT = "text"
    const val MESSAGE_TYPE_IMAGE = "image"
    const val MESSAGE_TYPE_FILE = "file"
    const val MESSAGE_TYPE_AUDIO = "audio"
    const val MESSAGE_TYPE_VIDEO = "video"
    const val MESSAGE_TYPE_SYSTEM = "system"

    // Действия обновления чата
    const val ACTION_MEMBER_ADDED = "member_added"
    const val ACTION_MEMBER_REMOVED = "member_removed"
    const val ACTION_NAME_CHANGED = "name_changed"
    const val ACTION_DESCRIPTION_CHANGED = "description_changed"

    // Коды ошибок
    const val ERROR_INVALID_TOKEN = "invalid_token"
    const val ERROR_TOKEN_EXPIRED = "token_expired"
    const val ERROR_UNAUTHORIZED = "unauthorized"
    const val ERROR_INVALID_MESSAGE = "invalid_message"
    const val ERROR_SERVER_ERROR = "server_error"
    const val ERROR_CONNECTION_LOST = "connection_lost"
}