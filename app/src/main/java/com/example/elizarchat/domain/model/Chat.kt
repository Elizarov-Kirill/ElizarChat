package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * Domain модель чата.
 * ID хранятся как String для гибкости, но конвертируются из/в Int.
 */
data class Chat(
    // ============ СЕРВЕРНЫЕ ПОЛЯ ============
    val id: String,                    // Конвертируется из Int
    val type: String,                  // "private", "group", "channel"
    val name: String? = null,
    val avatarUrl: String? = null,
    val createdBy: String,             // User ID создателя (конвертируется из Int)
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val lastMessageAt: Instant? = null, // Когда было последнее сообщение

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ ============
    val lastMessageId: String? = null, // Локальная ссылка на последнее сообщение
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val lastSyncAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // ============ ОТНОШЕНИЯ (загружаются отдельно) ============
    val participants: List<ChatMember> = emptyList(),
    val lastMessage: Message? = null,
    val creator: User? = null
) {
    /**
     * Вычисляемое свойство для отображения названия чата в UI.
     * Для групповых чатов - имя чата, для личных - имя собеседника.
     */
    fun displayName(currentUserId: String?): String = when (type) {
        "private" -> {
            // Для личных чатов показываем имя собеседника
            participants.firstOrNull { it.user?.id != currentUserId }?.user?.displayNameOrUsername
                ?: name ?: "Unknown"
        }
        else -> name ?: "Group Chat"
    }

    /**
     * Проверяет, является ли чат групповым
     */
    val isGroupChat: Boolean
        get() = type == "group" || type == "channel"

    /**
     * Проверяет, является ли чат приватным
     */
    val isPrivateChat: Boolean
        get() = type == "private"

    /**
     * Количество участников (исключая текущего пользователя)
     */
    fun participantCount(currentUserId: String?): Int =
        participants.count { it.user?.id != currentUserId }

    /**
     * Общее количество непрочитанных сообщений для текущего пользователя
     * (вычисляется из участников чата)
     */
    fun unreadCountForUser(userId: String): Int =
        participants.find { it.user?.id == userId }?.unreadCount ?: 0

    /**
     * Время последней активности
     */
    val lastActivityAt: Instant?
        get() = lastMessageAt ?: updatedAt ?: createdAt

    /**
     * Получает роль пользователя в чате
     */
    fun getUserRole(userId: String): String? =
        participants.find { it.user?.id == userId }?.role

    /**
     * Проверяет, является ли пользователь участником чата
     */
    fun isUserMember(userId: String): Boolean =
        participants.any { it.user?.id == userId }
}

/**
 * Участник чата (связь многие-ко-многим)
 */
data class ChatMember(
    val id: String,                    // Конвертируется из Int (было Long)
    val chatId: String,                // Конвертируется из Int
    val userId: String,                // Конвертируется из Int
    val role: String,                  // "owner", "admin", "member", "guest"
    val joinedAt: Instant,
    val unreadCount: Int = 0,          // Добавлено! Количество непрочитанных для этого участника
    val lastReadMessageId: String? = null, // Конвертируется из Int?
    val notificationsEnabled: Boolean = true,
    val isHidden: Boolean = false,

    // Отношения (загружаются отдельно)
    val user: User? = null
)

/**
 * Статус синхронизации
 */


/**
 * Краткое представление чата для списков
 */
data class ChatPreview(
    val id: String,
    val name: String,
    val type: String,
    val avatarUrl: String?,
    val lastMessageText: String?,
    val lastMessageTime: Instant?,
    val unreadCount: Int = 0,          // Для текущего пользователя
    val isMuted: Boolean = false,
    val participantCount: Int = 0
)