package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * ДОМЕННАЯ МОДЕЛЬ: Чистое представление пользователя для бизнес-логики.
 * Обновлена согласно серверной спецификации.
 * ID хранятся как String для гибкости, но конвертируются из/в Int.
 */
data class User(
    // ============ СЕРВЕРНЫЕ ПОЛЯ ============
    val id: String,                    // Конвертируется из Int
    val username: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val status: String? = null,        // "online", "offline", "busy", "away" (переименовано из statusText!)
    val isOnline: Boolean = false,
    val lastSeen: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant? = null,    // Добавлено!
    val settingsJson: String? = null,  // JSON строка с настройками

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ ============
    val isCurrentUser: Boolean = false,
    val isContact: Boolean = false,
    val isBlocked: Boolean = false,
    val isFavorite: Boolean = false,
    val lastSyncAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // ============ ВЫЧИСЛЯЕМЫЕ/ПРОИЗВОДНЫЕ ПОЛЯ ============
    val settings: UserSettings? = null // Парсится из settingsJson
) {
    /**
     * Вычисляемое свойство для UI.
     * Отображает displayName, если есть, иначе username.
     */
    val displayNameOrUsername: String
        get() = displayName ?: username

    /**
     * Статус для отображения в интерфейсе.
     */
    val uiStatus: UserStatus
        get() = when {
            isOnline -> UserStatus.ONLINE
            lastSeen == null -> UserStatus.OFFLINE
            Instant.now().epochSecond - lastSeen.epochSecond < 300 -> UserStatus.RECENTLY
            status == "busy" -> UserStatus.BUSY
            status == "away" -> UserStatus.AWAY
            else -> UserStatus.OFFLINE
        }

    /**
     * Получает значение настройки пользователя
     */
    fun getSetting(key: String, defaultValue: String = ""): String {
        return settings?.get(key) ?: defaultValue
    }

    /**
     * Проверяет, активен ли пользователь (был онлайн недавно)
     */
    val isRecentlyActive: Boolean
        get() = isOnline || (lastSeen != null &&
                Instant.now().epochSecond - lastSeen.epochSecond < 3600) // 1 час
}

/**
 * Статусы пользователя для UI.
 */
enum class UserStatus {
    ONLINE, RECENTLY, OFFLINE, BUSY, AWAY
}

/**
 * Настройки пользователя (парсится из JSON)
 */
data class UserSettings(
    val notifications: Boolean = true,
    val theme: String = "light",
    val language: String = "en",
    val messageSounds: Boolean = true,
    val vibration: Boolean = true,
    val privacyShowOnline: Boolean = true,
    val privacyShowLastSeen: Boolean = true,
    // Дополнительные настройки хранятся как Map
    val custom: Map<String, String> = emptyMap()
) {
    operator fun get(key: String): String? = when (key) {
        "notifications" -> notifications.toString()
        "theme" -> theme
        "language" -> language
        "messageSounds" -> messageSounds.toString()
        "vibration" -> vibration.toString()
        "privacyShowOnline" -> privacyShowOnline.toString()
        "privacyShowLastSeen" -> privacyShowLastSeen.toString()
        else -> custom[key]
    }
}

/**
 * Краткое представление пользователя для списков.
 */
data class UserPreview(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val status: String?
)
