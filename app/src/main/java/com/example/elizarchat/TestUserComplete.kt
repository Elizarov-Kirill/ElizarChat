package com.example.elizarchat

import com.example.elizarchat.data.mapper.UserMapper
import com.example.elizarchat.data.remote.dto.UserDto
import com.example.elizarchat.domain.model.User
import java.time.Instant

object TestUserComplete {
    fun runTest() {
        println("\n=== ПОЛНЫЙ ТЕСТ СУЩНОСТИ USER ===")

        // 1. Симуляция ответа API
        val apiResponse = """
            {
                "id": 123,
                "username": "john_doe",
                "email": "john@example.com",
                "displayName": "John Doe",
                "isOnline": true,
                "lastSeen": "2024-01-15T14:30:00Z",
                "createdAt": "2024-01-01T00:00:00Z"
            }
        """
        println("1. API ответ (JSON): $apiResponse")

        // 2. Retrofit создаёт DTO (kotlinx.serialization)
        val userDto = UserDto(
            id = 123L,  // Изменено: Long вместо String
            username = "john_doe",
            email = "john@example.com",
            displayName = "John Doe",
            // avatarUrl удален - его нет в DTO
            isOnline = true,
            lastSeen = "2024-01-15T14:30:00Z",
            createdAt = "2024-01-01T00:00:00Z"
        )
        println("\n2. DTO создан:")
        println("   - id: ${userDto.id}")
        println("   - username: ${userDto.username}")
        println("   - displayName: ${userDto.displayName}")
        println("   - email: ${userDto.email}")
        println("   - isOnline: ${userDto.isOnline}")
        println("   - last_seen (строка): ${userDto.lastSeen}")
        println("   - created_at (строка): ${userDto.createdAt}")

        // 3. Преобразуем в Domain модель
        val domainUser = UserMapper.dtoToDomain(userDto, isCurrentUser = true)
        println("\n3. Domain модель:")
        println("   - ID: ${domainUser.id}")
        println("   - username: ${domainUser.username}")
        println("   - Отображаемое имя: ${domainUser.displayName}")
        println("   - Email: ${domainUser.email}")
        println("   - Статус онлайн: ${domainUser.isOnline}")
        println("   - Last seen: ${domainUser.lastSeen}")
        println("   - Это текущий пользователь? ${domainUser.isCurrentUser}")

        // 4. Преобразуем в Entity для сохранения
        val userEntity = UserMapper.dtoToEntity(userDto)
        // Копируем с дополнительными полями
        val userEntityWithMetadata = userEntity.copy(
            isContact = true,
            isFavorite = true
        )
        println("\n4. Entity для Room:")
        println("   - ID: ${userEntityWithMetadata.id}")
        println("   - username: ${userEntityWithMetadata.username}")
        println("   - displayName: ${userEntityWithMetadata.displayName}")
        println("   - В контактах? ${userEntityWithMetadata.isContact}")
        println("   - В избранном? ${userEntityWithMetadata.isFavorite}")

        // 5. Обратное преобразование Entity → Domain
        val domainFromEntity = UserMapper.entityToDomain(userEntityWithMetadata)
        println("\n5. Entity → Domain:")
        println("   - ID: ${domainFromEntity.id}")
        println("   - Имя пользователя: ${domainFromEntity.username}")
        println("   - Отображаемое имя: ${domainFromEntity.displayName}")
        println("   - Все поля совпадают? ${domainUser.id == domainFromEntity.id}")

        println("\n=== ТЕСТ ЗАВЕРШЕН ===\n")
    }
}