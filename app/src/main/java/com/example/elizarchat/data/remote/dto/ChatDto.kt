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

    @SerialName("avatar_url")
    val avatarUrl: String? = null,  // TEXT (добавлено!)

    @SerialName("created_by")
    val createdBy: Int,  // INTEGER REFERENCES users(id)

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("updated_at")
    val updatedAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("last_message_at")
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

    @SerialName("chat_id")
    val chatId: Int,  // INTEGER REFERENCES chats(id)

    @SerialName("user_id")
    val userId: Int,  // INTEGER REFERENCES users(id)

    @SerialName("role")
    val role: String,  // "owner", "admin", "member", "guest"

    @SerialName("joined_at")
    val joinedAt: String? = null,  // ISO 8601 строка (TIMESTAMP)

    @SerialName("unread_count")
    val unreadCount: Int = 0,  // INTEGER DEFAULT 0 (добавлено!)

    @SerialName("last_read_message_id")
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

    @SerialName("avatar_url")
    val avatarUrl: String? = null,  // Добавлено!

    @SerialName("member_ids")
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

    @SerialName("avatar_url")
    val avatarUrl: String? = null  // Добавлено!
)

/**
 * Добавление участника
 * POST /api/v1/chats/:id/members
 */
@Serializable
data class AddMemberRequest(
    @SerialName("user_id")
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
    @SerialName("user_id")
    val userId: Int
)

/**
 * Запрос на обновление unread_count
 */
@Serializable
data class UpdateUnreadCountRequest(
    @SerialName("unread_count")
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

    @SerialName("has_more_messages")
    val hasMoreMessages: Boolean = false
)