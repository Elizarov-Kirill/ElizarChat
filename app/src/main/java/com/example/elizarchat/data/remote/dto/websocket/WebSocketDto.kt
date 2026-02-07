// websocket/WebSocketDto.kt
package com.example.elizarchat.data.remote.dto.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement  // ДОБАВЛЕНО

// ============== ВХОДЯЩИЕ СООБЩЕНИЯ (клиент → сервер) ==============

@Serializable
sealed class WebSocketIncomingMessage {
    abstract val type: String
}

@Serializable
data class PingMessage(
    @SerialName("type")
    override val type: String = "ping"
) : WebSocketIncomingMessage()

@Serializable
data class SendMessageRequest(
    @SerialName("type")
    override val type: String = "send_message",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("content")
    val content: String,

    @SerialName("replyTo")  // ✅ camelCase
    val replyTo: Int? = null,

    @SerialName("messageType")  // ✅ camelCase
    val messageType: String = "text"
) : WebSocketIncomingMessage()

@Serializable
data class TypingMessage(
    @SerialName("type")
    override val type: String = "typing",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("isTyping")  // ✅ camelCase
    val isTyping: Boolean
) : WebSocketIncomingMessage()

@Serializable
data class ReadReceiptMessage(
    @SerialName("type")
    override val type: String = "read_receipt",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("messageIds")  // ✅ camelCase
    val messageIds: List<Int>
) : WebSocketIncomingMessage()

@Serializable
data class SubscribeChatMessage(
    @SerialName("type")
    override val type: String = "subscribe_chat",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int
) : WebSocketIncomingMessage()

@Serializable
data class UnsubscribeChatMessage(
    @SerialName("type")
    override val type: String = "unsubscribe_chat",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int
) : WebSocketIncomingMessage()

// ============== ИСХОДЯЩИЕ СООБЩЕНИЯ (сервер → клиент) ==============

@Serializable
sealed class WebSocketOutgoingMessage {
    abstract val type: String
    abstract val timestamp: String?
}

@Serializable
data class WelcomeMessage(
    @SerialName("type")
    override val type: String = "welcome",

    @SerialName("message")
    val message: String,

    @SerialName("user")
    val user: WelcomeUser,

    @SerialName("serverInfo")  // ✅ camelCase
    val serverInfo: ServerInfo,

    @SerialName("chats")
    val chats: List<WelcomeChat>,

    @SerialName("timestamp")
    override val timestamp: String? = null
) : WebSocketOutgoingMessage()

@Serializable
data class WelcomeUser(
    @SerialName("id")
    val id: Int,

    @SerialName("email")
    val email: String,

    @SerialName("username")
    val username: String,

    @SerialName("displayName")  // ✅ camelCase
    val displayName: String? = null
)

@Serializable
data class ServerInfo(
    @SerialName("version")
    val version: String,

    @SerialName("timestamp")
    val timestamp: String,

    @SerialName("connectionId")  // ✅ camelCase
    val connectionId: String
)

@Serializable
data class WelcomeChat(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String? = null,

    @SerialName("type")
    val type: String,

    @SerialName("unreadCount")  // ✅ camelCase
    val unreadCount: Int = 0,

    @SerialName("lastMessageAt")  // ✅ camelCase
    val lastMessageAt: String? = null
)

@Serializable
data class PongMessage(
    @SerialName("type")
    override val type: String = "pong",

    @SerialName("timestamp")
    override val timestamp: String,

    @SerialName("serverTime")  // ✅ camelCase
    val serverTime: Long
) : WebSocketOutgoingMessage()

@Serializable
data class MessageSentConfirmation(
    @SerialName("type")
    override val type: String = "message_sent",

    @SerialName("messageId")  // ✅ camelCase
    val messageId: Int,

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class NewMessageEvent(
    @SerialName("type")
    override val type: String = "new_message",

    @SerialName("message")
    val message: com.example.elizarchat.data.remote.dto.MessageDto,

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("senderId")  // ✅ camelCase
    val senderId: Int,

    @SerialName("senderEmail")  // ✅ camelCase
    val senderEmail: String,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class UserTypingEvent(
    @SerialName("type")
    override val type: String = "user_typing",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("userId")  // ✅ camelCase
    val userId: Int,

    @SerialName("userEmail")  // ✅ camelCase
    val userEmail: String,

    @SerialName("isTyping")  // ✅ camelCase
    val isTyping: Boolean,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class ReadReceiptAck(
    @SerialName("type")
    override val type: String = "read_receipt_ack",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("messageIds")  // ✅ camelCase
    val messageIds: List<Int>,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class ChatSubscribedConfirmation(
    @SerialName("type")
    override val type: String = "chat_subscribed",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class ChatUnsubscribedConfirmation(
    @SerialName("type")
    override val type: String = "chat_unsubscribed",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class ErrorMessage(
    @SerialName("type")
    override val type: String = "error",

    @SerialName("code")
    val code: String,

    @SerialName("message")
    val message: String,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

// ============== ДОПОЛНИТЕЛЬНЫЕ СОБЫТИЯ ИЗ chatHandler.js ==============

@Serializable
data class MessageUpdatedEvent(
    @SerialName("type")
    override val type: String = "message_updated",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("messageId")  // ✅ camelCase
    val messageId: Int,

    @SerialName("updates")
    val updates: Map<String, JsonElement> = emptyMap(),  // ИСПРАВЛЕНО: Any → JsonElement

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class MessageDeletedEvent(
    @SerialName("type")
    override val type: String = "message_deleted",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("messageId")  // ✅ camelCase
    val messageId: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class ChatUpdatedEvent(
    @SerialName("type")
    override val type: String = "chat_updated",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("updates")
    val updates: Map<String, JsonElement> = emptyMap(),  // ИСПРАВЛЕНО: Any → JsonElement

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class MemberAddedEvent(
    @SerialName("type")
    override val type: String = "member_added",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("userId")  // ✅ camelCase
    val userId: Int,

    @SerialName("addedBy")  // ✅ camelCase
    val addedBy: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class MemberRemovedEvent(
    @SerialName("type")
    override val type: String = "member_removed",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("userId")  // ✅ camelCase
    val userId: Int,

    @SerialName("removedBy")  // ✅ camelCase
    val removedBy: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class AddedToChatEvent(
    @SerialName("type")
    override val type: String = "added_to_chat",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("addedBy")  // ✅ camelCase
    val addedBy: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()

@Serializable
data class RemovedFromChatEvent(
    @SerialName("type")
    override val type: String = "removed_from_chat",

    @SerialName("chatId")  // ✅ camelCase
    val chatId: Int,

    @SerialName("removedBy")  // ✅ camelCase
    val removedBy: Int,

    @SerialName("timestamp")
    override val timestamp: String
) : WebSocketOutgoingMessage()