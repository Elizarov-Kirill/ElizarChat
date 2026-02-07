package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.UserEntity
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.User
import com.example.elizarchat.domain.model.UserSettings
import com.example.elizarchat.domain.model.SyncStatus
import java.time.Instant
import java.time.format.DateTimeParseException

object UserMapper {

    // === DTO → Entity ===
    fun dtoToEntity(dto: UserDto): UserEntity {
        return UserEntity(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = dto.id,
            username = dto.username,
            email = dto.email ?: "",
            displayName = dto.displayName,  // Исправлено: display_name → displayName
            avatarUrl = dto.avatarUrl,      // Исправлено: avatar_url → avatarUrl
            bio = dto.bio,
            status = dto.status,
            isOnline = dto.isOnline,        // Исправлено: is_online → isOnline
            lastSeen = parseInstant(dto.lastSeen),  // Исправлено: last_seen → lastSeen
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),  // Исправлено
            updatedAt = parseInstant(dto.updatedAt),  // Исправлено: updated_at → updatedAt
            settings = dto.settings,

            // ЛОКАЛЬНЫЕ ПОЛЯ (по умолчанию)
            isContact = false,
            contactNickname = null,
            isBlocked = false,
            isFavorite = false,
            lastUpdated = Instant.now(),
            syncStatus = "SYNCED"
        )
    }

    // === Entity → Domain ===
    fun entityToDomain(entity: UserEntity): User {
        return User(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = entity.id.toString(),
            username = entity.username,
            email = entity.email,
            displayName = entity.displayName,
            avatarUrl = entity.avatarUrl,
            bio = entity.bio,
            status = entity.status,
            isOnline = entity.isOnline,
            lastSeen = entity.lastSeen,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            settingsJson = entity.settings,

            // ЛОКАЛЬНЫЕ ПОЛЯ
            isCurrentUser = false,
            isContact = entity.isContact,
            isBlocked = entity.isBlocked,
            isFavorite = entity.isFavorite,
            lastSyncAt = entity.lastUpdated,
            syncStatus = parseSyncStatus(entity.syncStatus),

            // ВЫЧИСЛЯЕМЫЕ ПОЛЯ
            settings = parseUserSettings(entity.settings)
        )
    }

    // === DTO → Domain ===
    fun dtoToDomain(dto: UserDto): User {
        return User(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = dto.id.toString(),
            username = dto.username,
            email = dto.email ?: "",
            displayName = dto.displayName,  // Исправлено
            avatarUrl = dto.avatarUrl,      // Исправлено
            bio = dto.bio,
            status = dto.status,
            isOnline = dto.isOnline,        // Исправлено
            lastSeen = parseInstant(dto.lastSeen),  // Исправлено
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),  // Исправлено
            updatedAt = parseInstant(dto.updatedAt),  // Исправлено
            settingsJson = dto.settings,

            // ЛОКАЛЬНЫЕ ПОЛЯ (по умолчанию)
            isCurrentUser = false,
            isContact = false,
            isBlocked = false,
            isFavorite = false,
            lastSyncAt = Instant.now(),
            syncStatus = SyncStatus.SYNCED,

            // ВЫЧИСЛЯЕМЫЕ ПОЛЯ
            settings = parseUserSettings(dto.settings)
        )
    }

    // === Domain → Entity ===
    fun domainToEntity(domain: User): UserEntity {
        return UserEntity(
            // СЕРВЕРНЫЕ ПОЛЯ
            id = domain.id.toIntOrNull() ?: 0,
            username = domain.username,
            email = domain.email,
            displayName = domain.displayName,
            avatarUrl = domain.avatarUrl,
            bio = domain.bio,
            status = domain.status,
            isOnline = domain.isOnline,
            lastSeen = domain.lastSeen,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            settings = domain.settingsJson,

            // ЛОКАЛЬНЫЕ ПОЛЯ
            isContact = domain.isContact,
            contactNickname = null,
            isBlocked = domain.isBlocked,
            isFavorite = domain.isFavorite,
            lastUpdated = domain.lastSyncAt,
            syncStatus = domain.syncStatus.toString()
        )
    }

    // === Вспомогательные методы ===
    private fun parseUserSettings(json: String?): UserSettings? {
        return try {
            if (json.isNullOrEmpty()) {
                UserSettings()
            } else {
                // TODO: Реализовать парсинг JSON
                UserSettings()
            }
        } catch (e: Exception) {
            UserSettings()
        }
    }

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
    fun dtosToEntities(dtos: List<UserDto>): List<UserEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun entitiesToDomains(entities: List<UserEntity>): List<User> {
        return entities.map { entityToDomain(it) }
    }

    fun dtosToDomains(dtos: List<UserDto>): List<User> {
        return dtos.map { dtoToDomain(it) }
    }
}