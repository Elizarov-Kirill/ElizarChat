package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * Domain модель чата.
 * Обновлена согласно серверной спецификации.
 */
data class Chat(
    val id: String,
    val type: ChatType,
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdBy: String,             // User ID создателя
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val lastMessageId: String? = null, // ID последнего сообщения

    // Локальные поля (не с сервера)
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val participants: List<ChatMember> = emptyList(),
    val lastMessage: Message? = null
) {
    /**
     * Вычисляемое свойство для отображения названия чата в UI.
     * Для групповых чатов - имя чата, для личных - имя собеседника.
     */
    fun displayName(currentUserId: String?): String = when (type) {
        ChatType.PRIVATE -> {
            // Для личных чатов показываем имя собеседника
            participants.firstOrNull { it.user.id != currentUserId }?.user?.displayNameOrUsername
                ?: name ?: "Unknown"
        }
        else -> name ?: "Group Chat"
    }

    /**
     * Проверяет, является ли чат групповым
     */
    val isGroupChat: Boolean
        get() = type == ChatType.GROUP || type == ChatType.CHANNEL

    /**
     * Количество участников (исключая текущего пользователя)
     */
    fun participantCount(currentUserId: String?): Int =
        participants.count { it.user.id != currentUserId }

    /**
     * Упрощенное свойство для UI: есть ли непрочитанные
     */
    val hasUnread: Boolean
        get() = unreadCount > 0

    /**
     * Время последней активности
     */
    val lastActivityAt: Instant?
        get() = updatedAt ?: createdAt
}

/**
 * Типы чатов согласно серверной БД
 */
enum class ChatType {
    PRIVATE,    // Личный чат (2 участника)
    GROUP,      // Групповой чат
    CHANNEL     // Канал (публичный)
}

/**
 * Участник чата (связь многие-ко-многим)
 */
data class ChatMember(
    val id: Long,
    val chatId: String,
    val user: User,
    val role: MemberRole,
    val joinedAt: Instant,
    val lastReadMessageId: String? = null
)

/**
 * Роли участников чата
 */
enum class MemberRole {
    OWNER,    // Владелец
    ADMIN,    // Администратор
    MEMBER,   // Участник
    GUEST     // Гость
}

/**
 * Краткое представление чата для списков
 */
data class ChatPreview(
    val id: String,
    val name: String,
    val type: ChatType,
    val avatarUrl: String?,
    val lastMessageText: String?,
    val lastMessageTime: Instant?,
    val unreadCount: Int,
    val isMuted: Boolean
)