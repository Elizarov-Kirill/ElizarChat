package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity - сущность для хранения в Room Database.
 * Соответствует таблице 'users' в PostgreSQL сервера.
 */
@Entity(tableName = "users")
@TypeConverters(InstantConverter::class)
data class UserEntity(
    @PrimaryKey
    val id: Int,

    // === ПОЛЯ С СЕРВЕРА ===
    val username: String,
    val email: String?,
    val displayName: String? = null,
    val avatarUrl: String? = null,

    // НОВЫЕ ПОЛЯ С СЕРВЕРА
    val bio: String? = null,                // биография пользователя
    val status: String? = null,             // текстовый статус (например, "В сети", "Не беспокоить")
    val isOnline: Boolean = false,
    val lastSeen: Instant? = null,
    val createdAt: Instant,
    val settings: String? = null,           // JSON строка с настройками

    // === ЛОКАЛЬНЫЕ ПОЛЯ (только для этого приложения) ===
    val isContact: Boolean = false,
    val contactNickname: String? = null,
    val isBlocked: Boolean = false,
    val isFavorite: Boolean = false,

    // === СЛУЖЕБНЫЕ ПОЛЯ ===
    val lastUpdated: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    enum class SyncStatus {
        SYNCED,      // Данные совпадают с сервером
        PENDING,     // Ожидает синхронизации
        DIRTY        // Локальные изменения
    }
}