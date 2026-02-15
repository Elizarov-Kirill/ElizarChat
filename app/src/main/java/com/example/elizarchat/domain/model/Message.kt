package com.example.elizarchat.domain.model

import kotlinx.serialization.json.JsonObject
import java.time.Instant

/**
 * Domain модель сообщения.
 * Используется в бизнес-логике и UI.
 * ID хранятся как String для гибкости, но конвертируются из/в Int.
 */
data class Message(
    // ============ СЕРВЕРНЫЕ ПОЛЯ ============
    val id: String,                    // Конвертируется из Int
    val chatId: String,                // Конвертируется из Int
    val senderId: String,              // Конвертируется из Int (переименовано из userId!)
    val content: String,
    val type: String,                  // "text", "image", "video", "file", "voice", "system"
    val metadata: JsonObject = JsonObject(emptyMap()),
    val replyTo: String? = null,       // Конвертируется из Int? (ID сообщения)
    val status: String? = null,        // "sending", "sent", "delivered", "read"
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val deletedAt: Instant? = null,    // Мягкое удаление
    val readBy: List<String> = emptyList(), // Список ID пользователей

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ ============
    val localStatus: MessageStatus = MessageStatus.SENT, // Локальный статус для UI
    val isSending: Boolean = false,
    val localId: String? = null,       // Временный ID до синхронизации
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // ============ ОТНОШЕНИЯ (опционально) ============
    val sender: User? = null,          // Загружается отдельно
    val repliedMessage: Message? = null // Загружается отдельно
) {
    /**
     * Можно ли редактировать сообщение
     */
    val canEdit: Boolean
        get() = type == "text" &&
                deletedAt == null &&
                !isSending &&
                localStatus != MessageStatus.ERROR

    /**
     * Можно ли удалить сообщение
     */
    val canDelete: Boolean
        get() = deletedAt == null

    /**
     * Это системное сообщение
     */
    val isSystemMessage: Boolean
        get() = type == "system"

    /**
     * Сообщение удалено
     */
    val isDeleted: Boolean
        get() = deletedAt != null

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
            "text" -> content.take(100)
            "image" -> "📷 Изображение"
            "video" -> "🎥 Видео"
            "file" -> "📎 Файл"
            "voice" -> "🎤 Голосовое"
            "system" -> "⚙️ $content"
            else -> content.take(50)
        }

    /**
     * Проверяет, прочитано ли сообщение пользователем
     */
    fun isReadBy(userId: String): Boolean = readBy.contains(userId)
}

/**
 * Локальные статусы сообщения (для UI)
 */
enum class MessageStatus {
    SENDING,     // Отправляется (локальный статус)
    SENT,        // Отправлено на сервер
    DELIVERED,   // Доставлено получателям
    READ,        // Прочитано получателями
    ERROR        // Ошибка отправки
}
