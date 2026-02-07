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
    val id: Int,  // INTEGER PRIMARY KEY

    @SerialName("type")
    val type: String,  // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,  // VARCHAR(255)

    @SerialName("avatarUrl")  // ✅ ИСПРАВЛЕНО: camelCase
    val avatarUrl: String? = null,  // TEXT (добавлено!)

    @SerialName("createdBy")  // ✅ ИСПРАВЛЕНО: camelCase
    val createdBy: Int,  // INTEGER REFERENCES users(id)

    @SerialName("createdAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val createdAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("updatedAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val updatedAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("lastMessageAt")  // ✅ ИСПРАВЛЕНО: camelCase
    val lastMessageAt: String? = null,  // ISO 8601 строка (TIMESTAMP) - изменено!

    // Опционально: участники чата
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
    @SerialName("type")
    val type: String,  // "private", "group", "channel"

    @SerialName("name")
    val name: String? = null,

    @SerialName("avatarUrl")  // ✅ ИСПРАВЛЕНО: camelCase
    val avatarUrl: String? = null,  // Добавлено!

    @SerialName("memberIds")  // ✅ ИСПРАВЛЕНО: camelCase
    val memberIds: List<Int>  // ID пользователей для добавления
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