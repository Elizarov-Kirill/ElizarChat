package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ CHAT DTOs ============

/**
 * DTO чата - точное соответствие серверному API
 * Согласно таблице: chats(id, type, name, avatar_url, created_by,
 * created_at, updated_at, last_message_at)
 */
@Serializable
data class ChatDto(
    @SerialName("id")
    val id: Int,

    @SerialName("type")
    val type: String, // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,

    @SerialName("description") // ✅ Сервер использует description, а не avatarUrl
    val description: String? = null,

    @SerialName("createdBy")
    val createdBy: Int,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("lastMessageAt")
    val lastMessageAt: String? = null,

    @SerialName("lastMessage")
    val lastMessage: MessageDto? = null,

    @SerialName("unreadCount")
    val unreadCount: Int = 0,

    // ✅ Сервер использует эту структуру в welcome сообщении WebSocket
    @SerialName("members")
    val members: List<ChatMemberDto>? = null
)

/**
 * DTO участника чата
 * Согласно таблице: chat_members(id, chat_id, user_id, role,
 * joined_at, unread_count, last_read_message_id)
 */
@Serializable
data class ChatMemberDto(
    @SerialName("id")
    val id: Int,  // SERIAL PRIMARY KEY (изменено: Long → Int!)

    @SerialName("chatId")  // ✅ ИСПРАВЛЕНО: camelCase
    val chatId: Int,  // INTEGER REFERENCES chats(id)

    @SerialName("userId")  // ✅ ИСПРАВЛЕНО: camelCase
    val userId: Int,  // INTEGER REFERENCES users(id)

    @SerialName("role")
    val role: String,  // "owner", "admin", "member", "guest"

    @SerialName("joinedAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val joinedAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("unreadCount")  // ✅ ИСПРАВЛЕНО: camelCase
    val unreadCount: Int = 0,  // INTEGER DEFAULT 0 (добавлено!)

    @SerialName("lastReadMessageId")  // ✅ ИСПРАВЛЕНО: camelCase
    val lastReadMessageId: Int? = null  // INTEGER REFERENCES messages(id)
)

// ============ CHAT REQUESTS ============

/**
 * Создание чата
 * POST /api/v1/chats
 */
@Serializable
data class CreateChatRequest(
    @SerialName("type") // ✅ server использует "type"
    val type: String = "private",  // "private", "group", "channel"

    @SerialName("name") // ✅ server использует "name"
    val name: String? = "",

    @SerialName("description") // ✅ server использует "description" (а не avatarUrl!)
    val description: String? = null,

    @SerialName("userIds") // ✅ server использует "userIds" (snake_case, но в JSON это camelCase)
    val userIds: List<Int> = emptyList()  // Изменено: memberIds → userIds
)

/**
 * Обновление чата
 * PUT /api/v1/chats/:id
 */
@Serializable
data class UpdateChatRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("avatarUrl")  // ✅ ИСПРАВЛЕНО: camelCase
    val avatarUrl: String? = null  // Добавлено!
)

/**
 * Добавление участника
 * POST /api/v1/chats/:id/members
 */
@Serializable
data class AddMemberRequest(
    @SerialName("userId")  // ✅ ИСПРАВЛЕНО: camelCase
    val userId: Int,

    @SerialName("role")
    val role: String = "member"  // "owner", "admin", "member", "guest"
)

/**
 * Удаление участника
 * DELETE /api/v1/chats/:id/members
 */
@Serializable
data class RemoveMemberRequest(
    @SerialName("userId")  // ✅ ИСПРАВЛЕНО: camelCase
    val userId: Int
)

/**
 * Запрос на обновление unread_count
 */
@Serializable
data class UpdateUnreadCountRequest(
    @SerialName("unreadCount")  // ✅ ИСПРАВЛЕНО: camelCase
    val unreadCount: Int
)

// ============ CHAT RESPONSES ============

/**
 * Ответ со списком чатов
 */
@Serializable
data class ChatsResponse(
    @SerialName("chats")
    val chats: List<ChatDto> = emptyList(),

    @SerialName("total")
    val total: Int = 0,

    @SerialName("page")
    val page: Int = 1,

    @SerialName("limit")
    val limit: Int = 20
)

/**
 * Детальный ответ с чатом
 */
@Serializable
data class ChatDetailResponse(
    @SerialName("chat")
    val chat: ChatDto,

    @SerialName("members")
    val members: List<ChatMemberDto>,

    @SerialName("messages")
    val messages: List<MessageDto> = emptyList(),

    @SerialName("hasMoreMessages")  // ✅ ИСПРАВЛЕНО: camelCase
    val hasMoreMessages: Boolean = false
)