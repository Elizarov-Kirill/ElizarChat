package com.example.elizarchat.data.local.dao

import androidx.room.*
import com.example.elizarchat.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // === CREATE ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    // === READ ===
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    // === НОВЫЕ МЕТОДЫ ДЛЯ СЕРВЕРНЫХ ПОЛЕЙ ===

    // Поиск по биографии и статусу
    @Query("SELECT * FROM users WHERE bio LIKE '%' || :query || '%' OR status LIKE '%' || :query || '%'")
    suspend fun searchInBioAndStatus(query: String): List<UserEntity>

    // Онлайн пользователи
    @Query("SELECT * FROM users WHERE isOnline = 1")
    suspend fun getOnlineUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE isOnline = 1")
    fun observeOnlineUsers(): Flow<List<UserEntity>>

    // Пользователи с определенным статусом
    @Query("SELECT * FROM users WHERE status = :status")
    suspend fun getUsersByStatus(status: String): List<UserEntity>

    // === МЕТОДЫ ДЛЯ ЛОКАЛЬНЫХ ПОЛЕЙ ===

    // Контакты
    @Query("SELECT * FROM users WHERE isContact = 1 ORDER BY COALESCE(displayName, username)")
    fun observeContacts(): Flow<List<UserEntity>>

    // Избранные
    @Query("SELECT * FROM users WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<UserEntity>>

    // Заблокированные
    @Query("SELECT * FROM users WHERE isBlocked = 1")
    fun observeBlockedUsers(): Flow<List<UserEntity>>

    // Поиск по всем полям
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR " +
            "displayName LIKE '%' || :query || '%' OR " +
            "email LIKE '%' || :query || '%' OR " +
            "bio LIKE '%' || :query || '%' OR " +
            "status LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<UserEntity>

    // === UPDATE ===
    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateStatus(userId: String, isOnline: Boolean, lastSeen: Long?)  // Instant? → Long?

    // Обновление локальных полей
    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    suspend fun updateContactStatus(userId: String, isContact: Boolean)

    @Query("UPDATE users SET isFavorite = :isFavorite WHERE id = :userId")
    suspend fun updateFavoriteStatus(userId: String, isFavorite: Boolean)

    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun updateBlockedStatus(userId: String, isBlocked: Boolean)

    // === DELETE ===
    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)
}