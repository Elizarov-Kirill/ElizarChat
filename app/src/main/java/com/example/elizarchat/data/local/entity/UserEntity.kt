package com.example.elizarchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity - сущность для хранения в Room Database.
 * Соответствует таблице 'users' в вашей PostgreSQL.
 * Добавляем локальные поля, которых нет на сервере.
 */
@Entity(tableName = "users")
@TypeConverters(InstantConverter::class)
data class UserEntity(
    @PrimaryKey
    val id: String,

    // Основные данные (из сервера)
    val username: String,
    val email: String?,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Instant? = null,
    val createdAt: Instant,

    // ЛОКАЛЬНЫЕ ПОЛЯ (только для этого приложения)
    val isContact: Boolean = false,
    val contactNickname: String? = null,
    val isBlocked: Boolean = false,
    val isFavorite: Boolean = false,

    // Служебные поля
    val lastUpdated: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    enum class SyncStatus {
        SYNCED,      // Данные совпадают с сервером
        PENDING,     // Ожидает синхронизации
        DIRTY        // Локальные изменения
    }
}