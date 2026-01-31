package com.example.elizarchat.data.local.entity

import androidx.room.*
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

/**
 * Entity - сущность для хранения в Room Database.
 * Соответствует таблице 'users' в PostgreSQL сервера.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["is_online"]),
        Index(value = ["last_seen"]),
        Index(value = ["last_updated"])
    ]
)
@TypeConverters(InstantConverter::class)
data class UserEntity(
    // ============ СЕРВЕРНЫЕ ПОЛЯ (синхронизированы с БД) ============
    @PrimaryKey
    val id: Int,  // INTEGER PRIMARY KEY

    @ColumnInfo(name = "username")
    val username: String,  // VARCHAR(255) NOT NULL

    @ColumnInfo(name = "email")
    val email: String,  // VARCHAR(255) NOT NULL (было nullable)

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,  // VARCHAR(255)

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,  // TEXT

    @ColumnInfo(name = "bio")
    val bio: String? = null,  // TEXT

    @ColumnInfo(name = "status")
    val status: String? = null,  // VARCHAR(50)

    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,  // BOOLEAN DEFAULT false

    @ColumnInfo(name = "last_seen")
    val lastSeen: Instant? = null,  // TIMESTAMP

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,  // TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant? = null,  // TIMESTAMP (добавлено)

    @ColumnInfo(name = "settings")
    val settings: String? = null,  // JSONB DEFAULT '{}'

    // ============ ЛОКАЛЬНЫЕ ПОЛЯ (только для клиента) ============
    @ColumnInfo(name = "is_contact")
    val isContact: Boolean = false,

    @ColumnInfo(name = "contact_nickname")
    val contactNickname: String? = null,

    @ColumnInfo(name = "is_blocked")
    val isBlocked: Boolean = false,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    // ============ СЛУЖЕБНЫЕ ПОЛЯ ============
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Instant = Instant.now(),  // Когда обновлялись локально

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "SYNCED"  // SYNCED, PENDING, DIRTY
)