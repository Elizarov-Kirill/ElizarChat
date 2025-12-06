package com.example.elizarchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.elizarchat.data.local.converter.InstantConverter
import java.time.Instant

@Entity(
    tableName = "chat_participants",
    primaryKeys = ["chatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chatId"]), // Индекс для быстрого поиска участников чата
        Index(value = ["userId"]), // Индекс для поиска чатов пользователя
        Index(value = ["chatId", "userId"], unique = true) // Уникальный индекс для primary key
    ]
)

data class ChatParticipantEntity(
    val chatId: String,
    val userId: String,
    val role: ParticipantRole = ParticipantRole.MEMBER,
    val joinedAt: Instant = Instant.now(),
    val unreadCount: Int = 0
)

enum class ParticipantRole {
    OWNER, ADMIN, MEMBER
}