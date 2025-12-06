package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * Domain модель сообщения.
 * Используется в бизнес-логике и UI.
 */
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val status: MessageStatus,
    val createdAt: Instant,
    val updatedAt: Instant? = null,

    // Отношения (опционально, загружаются отдельно)
    val sender: User? = null,
    val chat: Chat? = null,

    // Вложения
    val attachments: List<Attachment> = emptyList(),

    // Ответ на другое сообщение
    val replyTo: MessagePreview? = null,

    // Прочитано пользователями
    val readBy: List<String> = emptyList(),

    // Локальные свойства (не из API)
    val isSending: Boolean = false,
    val isFailed: Boolean = false,
    val localId: String? = null // Для временных сообщений
) {
    /**
     * Можно ли редактировать сообщение
     */
    val canEdit: Boolean
        get() = type == MessageType.TEXT &&
                status != MessageStatus.DELETED &&
                createdAt.isAfter(Instant.now().minusSeconds(3600)) // 1 час

    /**
     * Можно ли удалить сообщение
     */
    val canDelete: Boolean
        get() = status != MessageStatus.DELETED

    /**
     * Это системное сообщение
     */
    val isSystemMessage: Boolean
        get() = type == MessageType.SYSTEM

    /**
     * Это мое сообщение (для текущего пользователя)
     */
    fun isMine(currentUserId: String): Boolean =
        senderId == currentUserId

    /**
     * Короткое содержание для превью
     */
    val previewContent: String
        get() = when (type) {
            MessageType.TEXT -> content.take(100)
            MessageType.IMAGE -> "📷 Изображение"
            MessageType.VIDEO -> "🎥 Видео"
            MessageType.AUDIO -> "🎵 Аудио"
            MessageType.FILE -> "📎 Файл"
            MessageType.SYSTEM -> "⚙️ $content"
        }
}

/**
 * Статусы сообщения
 */
enum class MessageStatus {
    SENDING,     // Отправляется (локальный статус)
    SENT,        // Отправлено на сервер
    DELIVERED,   // Доставлено получателям
    READ,        // Прочитано получателями
    FAILED,      // Ошибка отправки
    DELETED      // Удалено
}

/**
 * Типы сообщений
 */
enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, SYSTEM
}

/**
 * Вложение к сообщению
 */
data class Attachment(
    val id: String,
    val url: String,
    val type: AttachmentType,
    val name: String? = null,
    val size: Long? = null,
    val duration: Long? = null, // Для аудио/видео в секундах
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null
) {
    /**
     * Форматированный размер файла
     */
    val formattedSize: String?
        get() = size?.let {
            when {
                it < 1024 -> "$it B"
                it < 1024 * 1024 -> "${it / 1024} KB"
                else -> "${it / (1024 * 1024)} MB"
            }
        }

    /**
     * Форматированная длительность
     */
    val formattedDuration: String?
        get() = duration?.let {
            val minutes = it / 60
            val seconds = it % 60
            "${minutes}:${seconds.toString().padStart(2, '0')}"
        }
}

/**
 * Типы вложений
 */
enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, FILE
}

/**
 * Упрощенный превью сообщения для чатов
 */
data class MessagePreview(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Instant,
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENT
)