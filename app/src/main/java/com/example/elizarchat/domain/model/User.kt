package com.example.elizarchat.domain.model

import java.time.Instant

/**
 * ДОМЕННАЯ МОДЕЛЬ: Чистое представление пользователя для бизнес-логики.
 * Не содержит аннотаций Room или Retrofit.
 * Используется в UseCases, ViewModels и UI-слое.
 */
data class User(
    // Основной идентификатор (UUID с сервера)
    val id: String,

    // Обязательные поля
    val username: String,
    val email: String?,

    // Опциональные поля (могут быть null)
    val displayName: String? = null,
    val avatarUrl: String? = null,

    // Статус онлайн/оффлайн
    val isOnline: Boolean = false,
    val lastSeen: Instant? = null,

    // Метаданные
    val createdAt: Instant,

    // Локальные вычисляемые свойства (не приходят с сервера)
    val isCurrentUser: Boolean = false
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
    val status: UserStatus
        get() = when {
            isOnline -> UserStatus.ONLINE
            lastSeen == null -> UserStatus.OFFLINE
            Instant.now().epochSecond - lastSeen.epochSecond < 300 -> UserStatus.RECENTLY
            else -> UserStatus.OFFLINE
        }
}

/**
 * Статусы пользователя для UI.
 */
enum class UserStatus {
    ONLINE, RECENTLY, OFFLINE, BUSY, AWAY
}

/**
 * Краткое представление пользователя для списков.
 */
data class UserPreview(
    val id: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val isOnline: Boolean
)