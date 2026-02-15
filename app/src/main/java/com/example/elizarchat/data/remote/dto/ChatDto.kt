package com.example.elizarchat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============ CHAT DTOs ============

/**
 * DTO чата - соответствует ответу сервера
 * Сервер может возвращать разные наборы полей в разных контекстах
 */
@Serializable
data class ChatDto(
    // Обязательные поля (всегда есть)
    @SerialName("id")
    val id: Int,

    @SerialName("type")
    val type: String,

    // Опциональные поля (могут отсутствовать)
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("createdBy")
    val createdBy: Int? = null,  // ← СДЕЛАНО NULLABLE!

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("updatedAt")
    val updatedAt: String? = null,

    @SerialName("lastMessageAt")
    val lastMessageAt: String? = null,

    @SerialName("lastMessage")
    val lastMessage: MessageDto? = null,

    @SerialName("unreadCount")
    val unreadCount: Int = 0,  // Значение по умолчанию

    @SerialName("members")
    val members: List<ChatMemberDto>? = null,

    @SerialName("avatarUrl")  // Добавляем если нужно
    val avatarUrl: String? = null
)

/**
 * DTO участника чата
 */
@Serializable
data class ChatMemberDto(
    @SerialName("id")
    val id: Int? = null,  // Может отсутствовать

    @SerialName("chatId")
    val chatId: Int? = null,

    @SerialName("userId")
    val userId: Int,

    @SerialName("role")
    val role: String = "member",  // Значение по умолчанию

    @SerialName("joinedAt")
    val joinedAt: String? = null,

    @SerialName("unreadCount")
    val unreadCount: Int = 0,

    @SerialName("lastReadMessageId")
    val lastReadMessageId: Int? = null
)

// ============ CHAT REQUESTS ============

/**
 * Создание чата
 */
@Serializable
data class CreateChatRequest(
    @SerialName("type")
    val type: String = "private",

    @SerialName("name")
    val name: String? = "",

    @SerialName("description")
    val description: String? = null,

    @SerialName("userIds")
    val userIds: List<Int> = emptyList()
)

/**
 * Обновление чата
 */
@Serializable
data class UpdateChatRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("avatarUrl")
    val avatarUrl: String? = null
)

/**
 * Добавление участника
 */
@Serializable
data class AddMemberRequest(
    @SerialName("userId")
    val userId: Int,

    @SerialName("role")
    val role: String = "member"
)

/**
 * Удаление участника
 */
@Serializable
data class RemoveMemberRequest(
    @SerialName("userId")
    val userId: Int
)

/**
 * Запрос на обновление unread_count
 */
@Serializable
data class UpdateUnreadCountRequest(
    @SerialName("unreadCount")
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

    @SerialName("hasMoreMessages")
    val hasMoreMessages: Boolean = false
)