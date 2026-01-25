package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * Domain модель сообщения.
 * Используется в бизнес-логике и UI.
 */
data class Message(
    val id: String,
    val chatId: String,
    val userId: String,  // Переименовали senderId → userId
    val content: String,
    val messageType: MessageType,
    val metadata: String? = null,  // JSON строка
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant? = null,

    // Локальные поля (не из API)
    val status: MessageStatus = MessageStatus.SENT,
    val isSending: Boolean = false,
    val isFailed: Boolean = false,
    val localId: String? = null,
    val replyTo: String? = null,

    // Отношения (опционально, загружаются отдельно)
    val sender: User? = null
) {
    /**
     * Можно ли редактировать сообщение
     */
    val canEdit: Boolean
        get() = messageType == MessageType.TEXT &&
                !isDeleted &&
                !isSending &&
                !isFailed

    /**
     * Можно ли удалить сообщение
     */
    val canDelete: Boolean
        get() = !isDeleted

    /**
     * Это системное сообщение
     */
    val isSystemMessage: Boolean
        get() = messageType == MessageType.SYSTEM

    /**
     * Это мое сообщение (для текущего пользователя)
     */
    fun isMine(currentUserId: String): Boolean =
        userId == currentUserId

    /**
     * Короткое содержание для превью
     */
    val previewContent: String
        get() = when (messageType) {
            MessageType.TEXT -> content.take(100)
            MessageType.IMAGE -> "📷 Изображение"
            MessageType.VIDEO -> "🎥 Видео"
            MessageType.FILE -> "📎 Файл"
            MessageType.VOICE -> "🎤 Голосовое"
            MessageType.SYSTEM -> "⚙️ $content"
        }
}

/**
 * Статусы сообщения (локальные)
 */
enum class MessageStatus {
    SENDING,     // Отправляется (локальный статус)
    SENT,        // Отправлено на сервер
    DELIVERED,   // Доставлено получателям
    READ,        // Прочитано получателями
    ERROR        // Ошибка отправки
}

/**
 * Типы сообщений
 */
enum class MessageType {
    TEXT, IMAGE, VIDEO, FILE, VOICE, SYSTEM
}