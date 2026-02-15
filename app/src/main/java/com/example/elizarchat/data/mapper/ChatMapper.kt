package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.ChatEntity
import com.example.elizarchat.data.remote.dto.ChatDto
import com.example.elizarchat.domain.model.Chat
import com.example.elizarchat.domain.model.SyncStatus
import java.time.Instant
import java.time.format.DateTimeParseException

object ChatMapper {

    // === DTO → Entity ===
    fun dtoToEntity(dto: ChatDto): ChatEntity {
        return ChatEntity(
            id = dto.id,
            type = dto.type,
            name = dto.name,
            avatarUrl = dto.avatarUrl,
            // Безопасно обрабатываем nullable поля
            createdBy = dto.createdBy ?: 0,
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            lastMessageAt = parseInstant(dto.lastMessageAt),
            isMuted = false,
            isPinned = false,
            lastSyncAt = Instant.now(),
            syncStatus = "SYNCED"
        )
    }

    // === Entity → Domain ===
    fun entityToDomain(entity: ChatEntity): Chat {
        return Chat(
            id = entity.id.toString(),
            type = entity.type,
            name = entity.name,
            avatarUrl = entity.avatarUrl,
            createdBy = entity.createdBy.toString(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            lastMessageAt = entity.lastMessageAt,
            lastMessageId = entity.lastMessageId?.toString(),
            isMuted = entity.isMuted,
            isPinned = entity.isPinned,
            lastSyncAt = entity.lastSyncAt,
            syncStatus = parseSyncStatus(entity.syncStatus)
        )
    }

    // === DTO → Domain ===
    fun dtoToDomain(dto: ChatDto): Chat {
        return Chat(
            id = dto.id.toString(),
            type = dto.type,
            name = dto.name,
            avatarUrl = dto.avatarUrl,
            createdBy = dto.createdBy?.toString() ?: "0",
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            updatedAt = parseInstant(dto.updatedAt),
            lastMessageAt = parseInstant(dto.lastMessageAt),
            isMuted = false,
            isPinned = false,
            lastSyncAt = Instant.now(),
            syncStatus = SyncStatus.SYNCED
        )
    }

    // === Domain → Entity ===
    fun domainToEntity(domain: Chat): ChatEntity {
        return ChatEntity(
            id = domain.id.toIntOrNull() ?: 0,
            type = domain.type,
            name = domain.name,
            avatarUrl = domain.avatarUrl,
            createdBy = domain.createdBy.toIntOrNull() ?: 0,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            lastMessageAt = domain.lastMessageAt,
            lastMessageId = domain.lastMessageId?.toIntOrNull(),
            isMuted = domain.isMuted,
            isPinned = domain.isPinned,
            lastSyncAt = domain.lastSyncAt,
            syncStatus = domain.syncStatus.toString()
        )
    }

    // === Вспомогательные методы ===
    private fun parseSyncStatus(status: String): SyncStatus {
        return when (status.uppercase()) {
            "SYNCED" -> SyncStatus.SYNCED
            "PENDING" -> SyncStatus.PENDING
            "DIRTY" -> SyncStatus.DIRTY
            else -> SyncStatus.SYNCED
        }
    }

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    // === Для массовой конвертации ===
    fun dtosToEntities(dtos: List<ChatDto>): List<ChatEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun entitiesToDomains(entities: List<ChatEntity>): List<Chat> {
        return entities.map { entityToDomain(it) }
    }

    fun dtosToDomains(dtos: List<ChatDto>): List<Chat> {
        return dtos.map { dtoToDomain(it) }
    }
}