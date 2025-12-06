package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.ChatEntity
import com.example.elizarchat.data.local.entity.ChatParticipantEntity
import com.example.elizarchat.data.local.entity.ParticipantRole
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.data.remote.dto.ChatWithParticipantsDto
import com.example.elizarchat.data.remote.dto.MessageDto
import com.example.elizarchat.domain.model.Chat
import com.example.elizarchat.domain.model.ChatType
import com.example.elizarchat.domain.model.MessagePreview
import com.example.elizarchat.domain.model.MessageType
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Маппер для преобразований Chat.
 */
object ChatMapper {

    // === Основные преобразования ===

    fun dtoToEntity(dto: ChatDto): ChatEntity {
        return ChatEntity(
            id = dto.id.toString(), // Конвертируем Long → String
            name = dto.name,
            type = parseChatType(dto.type),
            avatarUrl = dto.avatarUrl,
            createdBy = dto.createdBy?.toString(),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            lastMessageAt = parseInstant(dto.updatedAt), // Используем updatedAt как lastMessageAt
            unreadCount = dto.unreadCount,
            // Остальные поля оставляем со значениями по умолчанию
            isArchived = false,
            isMuted = false,
            isPinned = false,
            lastUpdated = Instant.now(),
            syncStatus = ChatEntity.SyncStatus.SYNCED
        )
    }

    fun entityToDomain(
        entity: ChatEntity,
        participants: List<com.example.elizarchat.domain.model.User> = emptyList(),
        lastMessage: MessagePreview? = null
    ): Chat {
        return Chat(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            avatarUrl = entity.avatarUrl,
            createdBy = entity.createdBy ?: "",
            createdAt = entity.createdAt,
            lastMessageAt = entity.lastMessageAt,
            unreadCount = entity.unreadCount,
            participants = participants,
            lastMessage = lastMessage
        )
    }

    fun dtoToDomain(
        dto: ChatDto,
        participants: List<com.example.elizarchat.domain.model.User> = emptyList(),
        lastMessage: MessagePreview? = null
    ): Chat {
        return Chat(
            id = dto.id.toString(), // Конвертируем Long → String
            name = dto.name,
            type = parseChatType(dto.type),
            avatarUrl = dto.avatarUrl,
            createdBy = dto.createdBy?.toString() ?: "",
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            lastMessageAt = parseInstant(dto.updatedAt), // Используем updatedAt как lastMessageAt
            unreadCount = dto.unreadCount,
            participants = participants,
            lastMessage = lastMessage
        )
    }

    // === Для ChatWithParticipantsDto ===

    fun fullDtoToDomain(
        dto: ChatWithParticipantsDto,
        userMapper: UserMapper = UserMapper
    ): Chat {
        val participants = userMapper.dtosToDomains(dto.participants)
        val lastMessage = dto.lastMessage?.let {
            MessagePreview(
                id = it.id.toString(),
                content = it.content,
                senderName = getSenderName(it.senderId, participants),
                timestamp = parseInstant(it.createdAt) ?: Instant.now(),
                type = parseMessageType(it.type)
            )
        }

        return Chat(
            id = dto.id.toString(), // Конвертируем Long → String
            name = dto.name,
            type = parseChatType(dto.type),
            avatarUrl = dto.avatarUrl,
            createdBy = dto.createdBy?.toString() ?: "",
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            lastMessageAt = parseInstant(dto.updatedAt), // Используем updatedAt как lastMessageAt
            unreadCount = dto.unreadCount,
            participants = participants,
            lastMessage = lastMessage
        )
    }

    // === Для ChatParticipantEntity ===

    fun createParticipantEntities(chatId: String, userIds: List<String>): List<ChatParticipantEntity> {
        return userIds.map { userId ->
            ChatParticipantEntity(
                chatId = chatId,
                userId = userId,
                role = if (userId == chatId.split("_").firstOrNull()) {
                    ParticipantRole.OWNER
                } else {
                    ParticipantRole.MEMBER
                },
                joinedAt = Instant.now()
            )
        }
    }

    // === Вспомогательные методы ===

    private fun parseChatType(typeString: String): ChatType {
        return when (typeString.lowercase()) {
            "private" -> ChatType.PRIVATE
            "group" -> ChatType.GROUP
            "channel" -> ChatType.CHANNEL
            else -> ChatType.GROUP
        }
    }

    private fun parseMessageType(typeString: String): MessageType {
        return when (typeString.lowercase()) {
            "image" -> MessageType.IMAGE
            "video" -> MessageType.VIDEO
            "audio" -> MessageType.AUDIO
            "file" -> MessageType.FILE
            "system" -> MessageType.SYSTEM
            else -> MessageType.TEXT
        }
    }

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun getSenderName(senderId: Long, participants: List<com.example.elizarchat.domain.model.User>): String {
        return participants.find { it.id == senderId.toString() }?.displayNameOrUsername ?: "Unknown"
    }

    // === Для работы со списками ===

    fun dtosToEntities(dtos: List<ChatDto>): List<ChatEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun entitiesToDomains(
        entities: List<ChatEntity>,
        participantsMap: Map<String, List<com.example.elizarchat.domain.model.User>> = emptyMap(),
        lastMessagesMap: Map<String, MessagePreview> = emptyMap()
    ): List<Chat> {
        return entities.map { entity ->
            entityToDomain(
                entity = entity,
                participants = participantsMap[entity.id] ?: emptyList(),
                lastMessage = lastMessagesMap[entity.id]
            )
        }
    }

    // === Обновление сущности ===

    fun updateEntity(existing: ChatEntity, dto: ChatDto): ChatEntity {
        return existing.copy(
            name = dto.name ?: existing.name,
            avatarUrl = dto.avatarUrl ?: existing.avatarUrl,
            lastMessageAt = parseInstant(dto.updatedAt) ?: existing.lastMessageAt, // Используем updatedAt
            unreadCount = dto.unreadCount,
            lastUpdated = Instant.now(),
            syncStatus = if (existing.syncStatus == ChatEntity.SyncStatus.SYNCED)
                ChatEntity.SyncStatus.SYNCED
            else
                existing.syncStatus
        )
    }
}