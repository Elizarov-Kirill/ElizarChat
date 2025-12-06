package com.example.elizarchat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.elizarchat.domain.model.ChatType
import java.time.Instant

@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["createdBy"]), // Индекс для createdBy (частые запросы)
        Index(value = ["lastMessageAt"]) // Для сортировки по последнему сообщению
    ]
)
data class ChatEntity(
    @PrimaryKey
    val id: String,

    // Основные данные
    val name: String? = null,
    val type: ChatType, // Теперь Room поймёт через ChatTypeConverter
    val avatarUrl: String? = null,
    val createdBy: String?,
    val createdAt: Instant,
    val lastMessageAt: Instant? = null,

    // Локальные поля
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,

    // Служебные поля
    val lastUpdated: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
) {
    enum class SyncStatus {
        SYNCED, PENDING, DIRTY
    }
}