package com.example.elizarchat.data.mapper

import com.example.elizarchat.data.local.entity.UserEntity
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.User
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Маппер для преобразования между всеми представлениями User.
 * Вся логика конвертации находится здесь.
 */
object UserMapper {

    // === Основные преобразования ===

    fun dtoToDomain(dto: UserDto, isCurrentUser: Boolean = false): User {
        return User(
            id = dto.id.toString(), // Конвертируем Long в String
            username = dto.username,
            email = dto.email.orEmpty(),
            displayName = dto.displayName.orEmpty(),
            avatarUrl = null, // В API нет avatarUrl
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now(),
            isCurrentUser = isCurrentUser
        )
    }

    fun dtoToEntity(dto: UserDto): UserEntity {
        return UserEntity(
            id = dto.id.toString(), // Конвертируем Long в String
            username = dto.username,
            email = dto.email.orEmpty(), // Преобразуем String? в String
            displayName = dto.displayName.orEmpty(), // Преобразуем String? в String
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen),
            createdAt = parseInstant(dto.createdAt) ?: Instant.now()
        )
    }

    fun entityToDomain(entity: UserEntity, isCurrentUser: Boolean = false): User {
        return User(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            displayName = entity.displayName,
            isOnline = entity.isOnline,
            lastSeen = entity.lastSeen,
            createdAt = entity.createdAt,
            isCurrentUser = isCurrentUser
        )
    }

    fun domainToEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            username = domain.username,
            email = domain.email,
            displayName = domain.displayName,
            isOnline = domain.isOnline,
            lastSeen = domain.lastSeen,
            createdAt = domain.createdAt
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

    // === Вспомогательные методы ===

    private fun parseInstant(isoString: String?): Instant? {
        return try {
            isoString?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Обновляет существующую Entity новыми данными из DTO
     */
    fun updateEntity(existing: UserEntity, dto: UserDto): UserEntity {
        return existing.copy(
            username = dto.username,
            email = dto.email ?: existing.email,
            displayName = dto.displayName ?: existing.displayName,
            isOnline = dto.isOnline,
            lastSeen = parseInstant(dto.lastSeen) ?: existing.lastSeen,
            createdAt = parseInstant(dto.createdAt) ?: existing.createdAt
        )
    }
}