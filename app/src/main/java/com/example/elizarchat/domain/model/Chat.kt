package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * Domain модель чата.
 * Чистый Kotlin, без аннотаций библиотек.
 */
data class Chat(
    val id: String,
    val name: String? = null,          // null для личных чатов
    val type: ChatType,
    val avatarUrl: String? = null,
    val createdBy: String,             // User ID создателя
    val createdAt: Instant,

    // Динамические данные
    val lastMessageAt: Instant? = null,
    val unreadCount: Int = 0,

    // Отношения
    val participants: List<User> = emptyList(),
    val lastMessage: MessagePreview? = null
) {
    /**
     * Вычисляемое свойство для отображения названия чата в UI.
     * Для групповых чатов - имя чата, для личных - имя собеседника.
     */
    fun displayName(currentUserId: String?): String = when (type) {
        ChatType.PRIVATE -> {
            // Для личных чатов показываем имя собеседника
            participants.firstOrNull { it.id != currentUserId }?.displayNameOrUsername
                ?: name ?: "Unknown"
        }
        else -> name ?: "Group Chat"
    }

    /**
     * Краткое описание для UI (участники или последнее сообщение)
     */
    fun subtitle(currentUserId: String?): String = when {
        lastMessage != null -> "${lastMessage.senderName}: ${lastMessage.content}"
        participants.isNotEmpty() -> {
            val participantNames = participants
                .filter { it.id != currentUserId }
                .take(3)
                .joinToString(", ") { it.displayNameOrUsername }
            if (participantNames.isNotEmpty()) participantNames else "No participants"
        }
        else -> "New chat"
    }

    /**
     * Количество участников (исключая текущего пользователя)
     */
    fun participantCount(currentUserId: String?): Int =
        participants.count { it.id != currentUserId }

    /**
     * Упрощенное свойство для UI: есть ли непрочитанные
     */
    val hasUnread: Boolean
        get() = unreadCount > 0
}

/**
 * Типы чатов согласно твоей БД
 */
enum class ChatType {
    PRIVATE,    // Личный чат (2 участника)
    GROUP,      // Групповой чат
    CHANNEL     // Канал (публичный)
}

/**
 * Предпросмотр последнего сообщения (для списка чатов)
 */
data class MessagePreview(
    val id: String,
    val content: String,
    val senderName: String,
    val timestamp: Instant,
    val type: MessageType = MessageType.TEXT
)

/**
 * Типы сообщений (будет расширено в модели Message)
 */
enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM
}