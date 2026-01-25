package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.UserEntity
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.User
import com.example.elizarchat.domain.model.UserPreview
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Маппер для преобразования между всеми представлениями User.
 * Обновлен с учетом серверной спецификации.
 */
object UserMapper {

    // === Основные преобразования ===

    fun dtoToDomain(dto: UserDto, isCurrentUser: Boolean = false): User {
        return User(
            id = dto.id.toString(), // Конвертируем в String
            username = dto.username,
            email = dto.email.orEmpty(),
            displayName = dto.displayName.orEmpty(),
            avatarUrl = dto.avatarUrl,
            bio = dto.bio,
            statusText = dto.status,
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            settings = dto.settings,
            isCurrentUser = isCurrentUser,
            // Локальные поля по умолчанию
            isContact = false,
            isBlocked = false,
            isFavorite = false
        )
    }

    fun dtoToEntity(dto: UserDto): UserEntity {
        return UserEntity(
            id = dto.id.toString(),
            username = dto.username,
            email = dto.email.orEmpty(),
            displayName = dto.displayName.orEmpty(),
            avatarUrl = dto.avatarUrl,
            bio = dto.bio,
            status = dto.status,
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            settings = dto.settings,
            // Локальные поля по умолчанию
            isContact = false,
            contactNickname = null,
            isBlocked = false,
            isFavorite = false,
            lastUpdated = Instant.now(),
            syncStatus = UserEntity.SyncStatus.SYNCED
        )
    }

    fun entityToDomain(entity: UserEntity, isCurrentUser: Boolean = false): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email ?: "",
            displayName = entity.displayName ?: "",
            avatarUrl = entity.avatarUrl,
            bio = entity.bio,
            statusText = entity.status,
            isOnline = entity.isOnline,
            lastSeen = entity.lastSeen,
            createdAt = entity.createdAt,
            settings = entity.settings,
            isCurrentUser = isCurrentUser,
            // Локальные поля из Entity
            isContact = entity.isContact,
            isBlocked = entity.isBlocked,
            isFavorite = entity.isFavorite
        )
    }

    fun domainToEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            username = domain.username,
            email = domain.email,
            displayName = domain.displayName,
            avatarUrl = domain.avatarUrl,
            bio = domain.bio,
            status = domain.statusText,
            isOnline = domain.isOnline,
            lastSeen = domain.lastSeen,
            createdAt = domain.createdAt,
            settings = domain.settings,
            // Локальные поля из Domain
            isContact = domain.isContact,
            contactNickname = null,
            isBlocked = domain.isBlocked,
            isFavorite = domain.isFavorite,
            lastUpdated = Instant.now(),
            syncStatus = if (domain.isContact || domain.isBlocked || domain.isFavorite) {
                UserEntity.SyncStatus.DIRTY
            } else {
                UserEntity.SyncStatus.SYNCED
            }
        )
    }

    // === Работа со списками ===

    fun dtosToDomains(dtos: List<UserDto>, currentUserId: String? = null): List<User> {
        return dtos.map { dto ->
            dtoToDomain(dto, isCurrentUser = dto.id.toString() == currentUserId)
        }
    }

    fun entitiesToDomains(entities: List<UserEntity>, currentUserId: String? = null): List<User> {
        return entities.map { entity ->
            entityToDomain(entity, isCurrentUser = entity.id == currentUserId)
        }
    }

    fun dtosToEntities(dtos: List<UserDto>): List<UserEntity> {
        return dtos.map { dtoToEntity(it) }
    }

    fun domainsToEntities(domains: List<User>): List<UserEntity> {
        return domains.map { domainToEntity(it) }
    }

    // === Вспомогательные методы ===

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Обновляет существующую Entity новыми данными из DTO
     * Сохраняет локальные поля (isContact, isBlocked, isFavorite)
     */
    fun updateEntity(existing: UserEntity, dto: UserDto): UserEntity {
        return existing.copy(
            username = dto.username,
            email = dto.email ?: existing.email,
            displayName = dto.displayName ?: existing.displayName,
            avatarUrl = dto.avatarUrl ?: existing.avatarUrl,
            bio = dto.bio ?: existing.bio,
            status = dto.status ?: existing.status,
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen) ?: existing.lastSeen,
            createdAt = parseInstant(dto.createdAt) ?: existing.createdAt,
            settings = dto.settings ?: existing.settings,
            lastUpdated = Instant.now(),
            syncStatus = if (existing.syncStatus == UserEntity.SyncStatus.DIRTY) {
                // Сохраняем DIRTY статус если были локальные изменения
                UserEntity.SyncStatus.DIRTY
            } else {
                UserEntity.SyncStatus.SYNCED
            }
        )
    }

    /**
     * Обновляет только локальные поля Entity
     */
    fun updateLocalFields(
        entity: UserEntity,
        isContact: Boolean? = null,
        isBlocked: Boolean? = null,
        isFavorite: Boolean? = null,
        contactNickname: String? = null
    ): UserEntity {
        return entity.copy(
            isContact = isContact ?: entity.isContact,
            isBlocked = isBlocked ?: entity.isBlocked,
            isFavorite = isFavorite ?: entity.isFavorite,
            contactNickname = contactNickname ?: entity.contactNickname,
            lastUpdated = Instant.now(),
            syncStatus = UserEntity.SyncStatus.DIRTY
        )
    }

    /**
     * Преобразует domain в UserPreview для списков
     */
    fun domainToPreview(domain: User): UserPreview {
        return UserPreview(
            id = domain.id,
            username = domain.username,
            displayName = domain.displayName,
            avatarUrl = domain.avatarUrl,
            isOnline = domain.isOnline
        )
    }

    fun domainsToPreviews(domains: List<User>): List<UserPreview> {
        return domains.map { domainToPreview(it) }
    }
}