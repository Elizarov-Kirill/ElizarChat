// 📁 data/remote/dto/websocket/WebSocketDto.kt
package com.example.elizarchat.data.remote.dto.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

// ========================
// ВХОДЯЩИЕ сообщения (от клиента к серверу)
// ========================

// data/remote/dto/websocket/WebSocketDto.kt

@Serializable
sealed class WebSocketIncomingMessage

@Serializable
@SerialName("ping")
data class PingMessage(
    @SerialName("type") val type: String = "ping"
) : WebSocketIncomingMessage()

@Serializable
@SerialName("typing")
data class TypingMessage(
    val chatId: Int,
    val isTyping: Boolean
) : WebSocketIncomingMessage()

@Serializable
@SerialName("send_message")
data class SendMessageRequest(
    val chatId: Int,
    val content: String,
    val messageType: String = "text",
    val replyTo: Int? = null,
    val metadata: String = "{}"
) : WebSocketIncomingMessage()

@Serializable
@SerialName("subscribe_chat")
data class SubscribeChatMessage(
    @SerialName("type") val type: String = "subscribe_chat",
    @SerialName("chatId") val chatId: Int
) : WebSocketIncomingMessage()

@Serializable
@SerialName("unsubscribe_chat")
data class UnsubscribeChatMessage(
    @SerialName("type") val type: String = "unsubscribe_chat",
    @SerialName("chatId") val chatId: Int
) : WebSocketIncomingMessage()

// Подтверждение прочтения
@Serializable
@SerialName("read_receipt")
data class ReadReceiptMessage(
    @SerialName("type") val type: String = "read_receipt",
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

// Новое сообщение
@Serializable
@SerialName("new_message")
data class NewMessageEvent(
    @SerialName("type") override val type: String = "new_message",
    @SerialName("chatId") val chatId: Int,
    @SerialName("message") val message: ChatMessage,
    @SerialName("senderId") val senderId: Int, // Добавлено из сервера
    @SerialName("senderEmail") val senderEmail: String? = null, // Добавлено из сервера
    @SerialName("timestamp") val timestamp: String // Добавлено из сервера
) : WebSocketOutgoingMessage()

// Сообщение о печатании
@Serializable
@SerialName("user_typing")
data class UserTypingEvent(
    @SerialName("type") override val type: String = "user_typing",
    @SerialName("chatId") val chatId: Int,
    @SerialName("userId") val userId: Int,
    @SerialName("userEmail") val userEmail: String? = null, // Добавлено из сервера
    @SerialName("isTyping") val isTyping: Boolean,
    @SerialName("timestamp") val timestamp: String // Добавлено из сервера
) : WebSocketOutgoingMessage()

// Подтверждение отправки сообщения
@Serializable
@SerialName("message_sent")
data class MessageSentConfirmation(
    @SerialName("type") override val type: String = "message_sent",
    @SerialName("messageId") val messageId: Int,
    @SerialName("chatId") val chatId: Int,
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение подписки на чат
@Serializable
@SerialName("chat_subscribed")
data class ChatSubscribed(
    @SerialName("type") override val type: String = "chat_subscribed",
    @SerialName("chatId") val chatId: Int,
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение отписки от чата
@Serializable
@SerialName("chat_unsubscribed")
data class ChatUnsubscribed(
    @SerialName("type") override val type: String = "chat_unsubscribed",
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
    @SerialName("timestamp") val timestamp: String
) : WebSocketOutgoingMessage()

// Pong (ответ на ping)
@Serializable
@SerialName("pong")
data class PongMessage(
    @SerialName("type") override val type: String = "pong",
    @SerialName("timestamp") val timestamp: String,
    @SerialName("serverTime") val serverTime: Long? = null // Добавлено из сервера
) : WebSocketOutgoingMessage()

// Ошибка
@Serializable
@SerialName("error")
data class ErrorMessage(
    @SerialName("type") override val type: String = "error",
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String,
    @SerialName("timestamp") val timestamp: String // Добавлено из сервера
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
    @SerialName("action") val action: String,
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
    @SerialName("senderId") val senderId: Int, // Сервер использует user_id, но в сообщениях может быть senderId
    @SerialName("user_id") val userId: Int? = null, // Альтернативное имя
    @SerialName("chatId") val chatId: Int,
    @SerialName("chat_id") val chatIdAlt: Int? = null, // Альтернативное имя
    @SerialName("type") val type: String = "text",
    @SerialName("message_type") val messageType: String? = null, // Альтернативное имя
    @SerialName("status") val status: String? = null,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("created_at") val createdAtAlt: String? = null, // Альтернативное имя
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("metadata") val metadata: Map<String, String>? = null,
    @SerialName("replyTo") val replyTo: Int? = null,
    @SerialName("reply_to_id") val replyToId: Int? = null // Альтернативное имя
) {
    // Helper для получения ID чата из разных полей
    fun getEffectiveChatId(): Int = chatId ?: chatIdAlt ?: 0

    // Helper для получения ID отправителя
    fun getEffectiveSenderId(): Int = senderId ?: userId ?: 0

    // Helper для получения времени создания
    fun getEffectiveCreatedAt(): String = createdAt ?: createdAtAlt ?: ""
}

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

// ========================
// УТИЛИТЫ ДЛЯ РАБОТЫ С WEBSOCKET
// ========================

object WebSocketMessageHelper {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // Функция для определения типа сообщения
    fun getMessageType(jsonString: String): String? {
        return try {
            val jsonElement = json.parseToJsonElement(jsonString)
            if (jsonElement is JsonObject) {
                jsonElement["type"]?.jsonPrimitive?.content
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Создание метаданных в формате JSON строки
    fun createMetadata(vararg pairs: Pair<String, String>): String {
        return if (pairs.isEmpty()) {
            "{}" // Пустой JSON объект
        } else {
            Json.encodeToString(pairs.toMap())
        }
    }

    // Десериализация входящего сообщения (от клиента к серверу)
    fun deserializeIncomingMessage(jsonString: String): WebSocketIncomingMessage? {
        return try {
            val type = getMessageType(jsonString) ?: return null
            val json = Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

            when (type) {
                "ping" -> json.decodeFromString(PingMessage.serializer(), jsonString)
                "typing" -> json.decodeFromString(TypingMessage.serializer(), jsonString)
                "send_message" -> json.decodeFromString(SendMessageRequest.serializer(), jsonString)
                "subscribe_chat" -> json.decodeFromString(SubscribeChatMessage.serializer(), jsonString)
                "unsubscribe_chat" -> json.decodeFromString(UnsubscribeChatMessage.serializer(), jsonString)
                "read_receipt" -> json.decodeFromString(ReadReceiptMessage.serializer(), jsonString)
                else -> null
            }
        } catch (e: Exception) {
            println("❌ Ошибка десериализации входящего сообщения: ${e.message}")
            null
        }
    }

    // Десериализация исходящего сообщения (от сервера к клиенту)
    fun deserializeOutgoingMessage(jsonString: String): WebSocketOutgoingMessage? {
        return try {
            val type = getMessageType(jsonString) ?: return null
            val json = Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

            when (type) {
                "welcome" -> json.decodeFromString(WelcomeMessage.serializer(), jsonString)
                "new_message" -> json.decodeFromString(NewMessageEvent.serializer(), jsonString)
                "user_typing" -> json.decodeFromString(UserTypingEvent.serializer(), jsonString)
                "message_sent" -> json.decodeFromString(MessageSentConfirmation.serializer(), jsonString)
                "chat_subscribed" -> json.decodeFromString(ChatSubscribed.serializer(), jsonString)
                "chat_unsubscribed" -> json.decodeFromString(ChatUnsubscribed.serializer(), jsonString)
                "read_receipt_ack" -> json.decodeFromString(ReadReceiptAck.serializer(), jsonString)
                "pong" -> json.decodeFromString(PongMessage.serializer(), jsonString)
                "error" -> json.decodeFromString(ErrorMessage.serializer(), jsonString)
                "system" -> json.decodeFromString(SystemMessage.serializer(), jsonString)
                "user_status" -> json.decodeFromString(UserStatusUpdate.serializer(), jsonString)
                "chat_update" -> json.decodeFromString(ChatUpdate.serializer(), jsonString)
                else -> {
                    println("⚠️ Неизвестный тип сообщения: $type")
                    null
                }
            }
        } catch (e: Exception) {
            println("❌ Ошибка десериализации исходящего сообщения: ${e.message}")
            println("📝 Сырое сообщение: ${jsonString.take(200)}...")
            null
        }
    }

    // Сериализация входящего сообщения
    fun serializeIncomingMessage(message: WebSocketIncomingMessage): String {
        return try {
            val json = Json {
                encodeDefaults = true
                classDiscriminator = "type"
            }
            json.encodeToString(message)
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
    // Типы сообщений (клиент -> сервер)
    const val TYPE_PING = "ping"
    const val TYPE_TYPING = "typing"
    const val TYPE_SEND_MESSAGE = "send_message" // ИСПРАВЛЕНО
    const val TYPE_SUBSCRIBE_CHAT = "subscribe_chat" // ИСПРАВЛЕНО
    const val TYPE_UNSUBSCRIBE_CHAT = "unsubscribe_chat" // ИСПРАВЛЕНО
    const val TYPE_READ_RECEIPT = "read_receipt"

    // Типы сообщений (сервер -> клиент)
    const val TYPE_WELCOME = "welcome"
    const val TYPE_NEW_MESSAGE = "new_message"
    const val TYPE_USER_TYPING = "user_typing"
    const val TYPE_MESSAGE_SENT = "message_sent"
    const val TYPE_CHAT_SUBSCRIBED = "chat_subscribed"
    const val TYPE_CHAT_UNSUBSCRIBED = "chat_unsubscribed"
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
    const val ERROR_UNKNOWN_MESSAGE_TYPE = "UNKNOWN_MESSAGE_TYPE"
    const val ERROR_SEND_MESSAGE_FAILED = "SEND_MESSAGE_FAILED"
    const val ERROR_NOT_CHAT_MEMBER = "NOT_CHAT_MEMBER"
    const val ERROR_SERVER_ERROR = "server_error"
    const val ERROR_CONNECTION_LOST = "connection_lost"
}