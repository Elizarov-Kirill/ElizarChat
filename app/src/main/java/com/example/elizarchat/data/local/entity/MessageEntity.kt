package com.example.elizarchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.elizarchat.data.local.converter.InstantConverter
import com.example.elizarchat.data.local.converter.StringListConverter
import com.example.elizarchat.domain.model.MessageStatus
import com.example.elizarchat.domain.model.MessageType
import java.time.Instant

/**
 * Entity для хранения сообщений в Room.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(InstantConverter::class, StringListConverter::class)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val status: MessageStatus,
    val createdAt: Instant,
    val updatedAt: Instant? = null,

    // Вложения (храним как JSON)
    val attachmentsJson: String? = null,

    // ID сообщения, на которое отвечаем
    val replyTo: String? = null,

    // Прочитано пользователями
    val readBy: List<Long> = emptyList(),

    // Локальные флаги
    val isSending: Boolean = false,
    val isFailed: Boolean = false,
    val localId: String? = null,

    // Служебные поля
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val lastUpdated: Instant = Instant.now()
) {
    enum class SyncStatus {
        SYNCED,        // Синхронизировано с сервером
        PENDING_SEND,  // Ожидает отправки
        PENDING_EDIT,  // Ожидает редактирования
        PENDING_DELETE // Ожидает удаления
    }
}