package com.example.elizarchat.data.remote.dto.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

// ========================
// ВХОДЯЩИЕ сообщения (от клиента к серверу)
// ========================

@Serializable
sealed class WebSocketIncomingMessage

@Serializable
@SerialName("ping")
data class PingMessage(
    val timestamp: String? = null  // Добавляем опциональный параметр
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
    @SerialName("messageType") val messageType: String = "text",  // Есть дефолтное значение
    @SerialName("replyTo") val replyTo: Int? = null,
    val metadata: String = "{}"  // Есть дефолтное значение
) : WebSocketIncomingMessage()

@Serializable
@SerialName("subscribe_chat")
data class SubscribeChatMessage(
    val chatId: Int
) : WebSocketIncomingMessage()

@Serializable
@SerialName("unsubscribe_chat")
data class UnsubscribeChatMessage(
    val chatId: Int
) : WebSocketIncomingMessage()

@Serializable
@SerialName("read_receipt")
data class ReadReceiptMessage(
    val chatId: Int,
    val messageIds: List<Int>
) : WebSocketIncomingMessage()

// ========================
// ИСХОДЯЩИЕ сообщения (от сервера к клиенту)
// ========================

@Serializable
sealed class WebSocketOutgoingMessage

// Welcome сообщение
@Serializable
@SerialName("welcome")
data class WelcomeMessage(
    val message: String,
    val user: WelcomeUser? = null,
    val chats: List<WelcomeChat> = emptyList(),
    val serverInfo: ServerInfo? = null
) : WebSocketOutgoingMessage()

// Новое сообщение
@Serializable
@SerialName("new_message")
data class NewMessageEvent(
    @SerialName("chatId")
    val chatId: Int? = null,

    @SerialName("chat_id")
    val chatIdAlt: Int? = null,

    @SerialName("senderId")
    val senderId: Int? = null,

    @SerialName("sender_id")
    val senderIdAlt: Int? = null,

    @SerialName("senderEmail")
    val senderEmail: String? = null,

    val timestamp: String? = null,

    val message: ChatMessage? = null,

    val data: ChatMessage? = null
) : WebSocketOutgoingMessage() {
    fun getEffectiveChatId(): Int = chatId ?: chatIdAlt ?: message?.getEffectiveChatId() ?: 0
    fun getEffectiveSenderId(): Int = senderId ?: senderIdAlt ?: message?.getEffectiveSenderId() ?: 0
}

// Модель сообщения
@Serializable
data class ChatMessage(
    val id: Int,

    @SerialName("chatId")
    val chatId: Int? = null,

    @SerialName("chat_id")
    val chatIdAlt: Int? = null,

    @SerialName("senderId")
    val senderId: Int? = null,

    @SerialName("sender_id")
    val senderIdAlt: Int? = null,

    @SerialName("sender_username")
    val senderUsername: String? = null,

    @SerialName("sender_display_name")
    val senderDisplayName: String? = null,

    @SerialName("sender_avatar_url")
    val senderAvatarUrl: String? = null,

    val content: String,

    val type: String = "text",

    @SerialName("message_type")
    val messageType: String? = null,

    val metadata: String? = "{}",

    @SerialName("reply_to")
    val replyTo: Int? = null,

    val status: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("created_at")
    val createdAtAlt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("updated_at")
    val updatedAtAlt: String? = null,

    @SerialName("deleted_at")
    val deletedAt: String? = null,

    @SerialName("is_edited")
    val isEdited: Boolean = false,

    @SerialName("read_by")
    val readBy: List<Int>? = emptyList(),

    @SerialName("read_count")
    val readCount: Int = 0
) {
    fun getEffectiveChatId(): Int = chatId ?: chatIdAlt ?: 0
    fun getEffectiveSenderId(): Int = senderId ?: senderIdAlt ?: 0
    fun getEffectiveCreatedAt(): String = createdAt ?: createdAtAlt ?: ""
}

// Подтверждение отправки
@Serializable
@SerialName("message_sent")
data class MessageSentConfirmation(
    @SerialName("messageId") val messageId: Int,
    @SerialName("chatId") val chatId: Int,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение подписки
@Serializable
@SerialName("chat_subscribed")
data class ChatSubscribed(
    @SerialName("chatId") val chatId: Int,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение отписки
@Serializable
@SerialName("chat_unsubscribed")
data class ChatUnsubscribed(
    @SerialName("chatId") val chatId: Int,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Подтверждение прочтения
@Serializable
@SerialName("read_receipt_ack")
data class ReadReceiptAck(
    @SerialName("chatId") val chatId: Int,
    @SerialName("messageIds") val messageIds: List<Int>,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Pong
@Serializable
@SerialName("pong")
data class PongMessage(
    val timestamp: String,
    @SerialName("serverTime") val serverTime: Long? = null
) : WebSocketOutgoingMessage()

// Ошибка
@Serializable
@SerialName("error")
data class ErrorMessage(
    val code: String? = null,
    val message: String,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Системное сообщение
@Serializable
@SerialName("system")
data class SystemMessage(
    val message: String,
    val data: Map<String, String>? = null
) : WebSocketOutgoingMessage()

// Печатание
@Serializable
@SerialName("user_typing")
data class UserTypingEvent(
    @SerialName("chatId") val chatId: Int,
    @SerialName("userId") val userId: Int,
    @SerialName("userEmail") val userEmail: String? = null,
    @SerialName("isTyping") val isTyping: Boolean,
    val timestamp: String
) : WebSocketOutgoingMessage()

// Статус пользователя
@Serializable
@SerialName("user_status")
data class UserStatusUpdate(
    @SerialName("userId") val userId: Int,
    @SerialName("isOnline") val isOnline: Boolean,
    val status: String? = null,
    @SerialName("lastSeen") val lastSeen: String? = null
) : WebSocketOutgoingMessage()

// Обновление чата
@Serializable
@SerialName("chat_update")
data class ChatUpdate(
    @SerialName("chatId") val chatId: Int,
    val action: String,
    val data: Map<String, String>? = null,
    val timestamp: String
) : WebSocketOutgoingMessage()

// ========================
// ВСПОМОГАТЕЛЬНЫЕ МОДЕЛИ
// ========================

@Serializable
data class WelcomeUser(
    val id: Int,
    val email: String,
    val username: String,
    @SerialName("displayName") val displayName: String? = null
)

@Serializable
data class WelcomeChat(
    val id: Int,
    val name: String,
    val type: String,
    @SerialName("unreadCount") val unreadCount: Int = 0,
    @SerialName("lastMessageAt") val lastMessageAt: String? = null
)

@Serializable
data class ServerInfo(
    val version: String,
    val timestamp: String,
    @SerialName("connectionId") val connectionId: String
)

// ========================
// УТИЛИТЫ
// ========================

object WebSocketMessageHelper {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        classDiscriminator = "type"
    }

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

    fun serializeIncomingMessage(message: WebSocketIncomingMessage): String {
        return try {
            val json = Json {
                encodeDefaults = true
                classDiscriminator = "type"
                explicitNulls = false
            }
            val result = json.encodeToString(message)
            println("📤 Сериализовано (${result.length} chars): $result")
            result
        } catch (e: Exception) {
            println("❌ Ошибка сериализации: ${e.message}")
            "{}"
        }
    }

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
            println("❌ Ошибка десериализации входящего: ${e.message}")
            null
        }
    }

    fun deserializeOutgoingMessage(jsonString: String): WebSocketOutgoingMessage? {
        return try {
            val type = getMessageType(jsonString) ?: return null
            val json = Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
                coerceInputValues = true
            }

            val message = when (type) {
                "welcome" -> json.decodeFromString(WelcomeMessage.serializer(), jsonString)
                "new_message" -> json.decodeFromString(NewMessageEvent.serializer(), jsonString)
                "message_sent" -> json.decodeFromString(MessageSentConfirmation.serializer(), jsonString)
                "chat_subscribed" -> json.decodeFromString(ChatSubscribed.serializer(), jsonString)
                "chat_unsubscribed" -> json.decodeFromString(ChatUnsubscribed.serializer(), jsonString)
                "read_receipt_ack" -> json.decodeFromString(ReadReceiptAck.serializer(), jsonString)
                "pong" -> json.decodeFromString(PongMessage.serializer(), jsonString)
                "error" -> json.decodeFromString(ErrorMessage.serializer(), jsonString)
                "system" -> json.decodeFromString(SystemMessage.serializer(), jsonString)
                "user_typing" -> json.decodeFromString(UserTypingEvent.serializer(), jsonString)
                "user_status" -> json.decodeFromString(UserStatusUpdate.serializer(), jsonString)
                "chat_update" -> json.decodeFromString(ChatUpdate.serializer(), jsonString)
                else -> {
                    println("⚠️ Неизвестный тип: $type")
                    null
                }
            }

            message
        } catch (e: Exception) {
            println("❌ Ошибка десериализации исходящего: ${e.message}")
            println("📝 Сырое: ${jsonString.take(200)}...")
            null
        }
    }
}