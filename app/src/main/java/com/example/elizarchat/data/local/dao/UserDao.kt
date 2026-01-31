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
    suspend fun getUserById(userId: Int): UserEntity?  // Изменено: String → Int

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: Int): Flow<UserEntity?>  // Изменено: String → Int

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    // Пользователи по нескольким ID
    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<Int>): List<UserEntity>  // Добавлено

    // Все пользователи
    @Query("SELECT * FROM users ORDER BY username")
    fun observeAllUsers(): Flow<List<UserEntity>>  // Добавлено

    // Пользователи, отсортированные по последней активности
    @Query("""
        SELECT * FROM users 
        ORDER BY 
            CASE WHEN is_online = 1 THEN 0 ELSE 1 END,
            last_seen DESC NULLS LAST,
            username ASC
    """)
    fun observeUsersByActivity(): Flow<List<UserEntity>>  // Добавлено

    // === ПОИСК И ФИЛЬТРАЦИЯ ===
    @Query("""
        SELECT * FROM users 
        WHERE username LIKE '%' || :query || '%' OR 
              display_name LIKE '%' || :query || '%' OR
              bio LIKE '%' || :query || '%' OR
              status LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN username LIKE :query || '%' THEN 0 ELSE 1 END,
            username
        LIMIT :limit
    """)
    suspend fun searchUsers(query: String, limit: Int = 50): List<UserEntity>

    // Онлайн пользователи
    @Query("SELECT * FROM users WHERE is_online = 1 ORDER BY username")
    suspend fun getOnlineUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE is_online = 1 ORDER BY username")
    fun observeOnlineUsers(): Flow<List<UserEntity>>

    // Пользователи с определенным статусом
    @Query("SELECT * FROM users WHERE status = :status ORDER BY username")
    suspend fun getUsersByStatus(status: String): List<UserEntity>

    // Недавно активные пользователи (были онлайн в последний час)
    @Query("""
        SELECT * FROM users 
        WHERE last_seen IS NOT NULL 
        AND last_seen > :sinceTimestamp
        ORDER BY last_seen DESC
    """)
    suspend fun getRecentlyActiveUsers(sinceTimestamp: Long): List<UserEntity>  // Добавлено

    // === ЛОКАЛЬНЫЕ ПОЛЯ ===
    @Query("SELECT * FROM users WHERE is_contact = 1 ORDER BY COALESCE(display_name, username)")
    fun observeContacts(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE is_favorite = 1 ORDER BY COALESCE(display_name, username)")
    fun observeFavorites(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE is_blocked = 1 ORDER BY username")
    fun observeBlockedUsers(): Flow<List<UserEntity>>

    // Пользователи, которых нет в контактах
    @Query("SELECT * FROM users WHERE is_contact = 0 AND id != :currentUserId ORDER BY username")
    fun observeNonContacts(currentUserId: Int): Flow<List<UserEntity>>  // Добавлено

    // === UPDATE ===
    @Update
    suspend fun update(user: UserEntity)

    // Обновление статуса онлайн
    @Query("UPDATE users SET is_online = :isOnline, last_seen = :lastSeen WHERE id = :userId")
    suspend fun updateStatus(userId: Int, isOnline: Boolean, lastSeen: Long?)  // Изменено: String → Int

    // Обновление профиля пользователя
    @Query("""
        UPDATE users SET 
            display_name = :displayName,
            avatar_url = :avatarUrl,
            bio = :bio,
            status = :userStatus,
            updated_at = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateProfile(
        userId: Int,  // Изменено: String → Int
        displayName: String?,
        avatarUrl: String?,
        bio: String?,
        userStatus: String?,
        updatedAt: Long
    )  // Добавлено

    // Обновление настроек
    @Query("UPDATE users SET settings = :settings, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updateSettings(userId: Int, settings: String?, updatedAt: Long)  // Добавлено

    // Обновление локальных полей
    @Query("UPDATE users SET is_contact = :isContact, contact_nickname = :nickname WHERE id = :userId")
    suspend fun updateContactStatus(userId: Int, isContact: Boolean, nickname: String? = null)  // Изменено: String → Int

    @Query("UPDATE users SET is_favorite = :isFavorite WHERE id = :userId")
    suspend fun updateFavoriteStatus(userId: Int, isFavorite: Boolean)  // Изменено: String → Int

    @Query("UPDATE users SET is_blocked = :isBlocked WHERE id = :userId")
    suspend fun updateBlockedStatus(userId: Int, isBlocked: Boolean)  // Изменено: String → Int

    // Обновление статуса синхронизации
    @Query("UPDATE users SET sync_status = :syncStatus, last_updated = :timestamp WHERE id = :userId")
    suspend fun updateSyncStatus(userId: Int, syncStatus: String, timestamp: Long = System.currentTimeMillis())  // Добавлено

    // Пометка как синхронизированного
    @Query("UPDATE users SET sync_status = 'SYNCED', last_updated = :timestamp WHERE id = :userId")
    suspend fun markAsSynced(userId: Int, timestamp: Long = System.currentTimeMillis())  // Добавлено

    // Обновление после серверной синхронизации
    @Query("""
        UPDATE users SET 
            username = :username,
            email = :email,
            display_name = :displayName,
            avatar_url = :avatarUrl,
            bio = :bio,
            status = :userStatus,
            is_online = :isOnline,
            last_seen = :lastSeen,
            settings = :settings,
            updated_at = :updatedAt,
            sync_status = 'SYNCED',
            last_updated = :syncTimestamp
        WHERE id = :userId
    """)
    suspend fun updateFromServer(
        userId: Int,
        username: String,
        email: String,
        displayName: String?,
        avatarUrl: String?,
        bio: String?,
        userStatus: String?,
        isOnline: Boolean,
        lastSeen: Long?,
        settings: String?,
        updatedAt: Long?,
        syncTimestamp: Long
    )  // Добавлено

    // === DELETE ===
    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: Int)  // Изменено: String → Int

    @Query("DELETE FROM users WHERE id IN (:userIds)")
    suspend fun deleteByIds(userIds: List<Int>)  // Добавлено

    // Удалить всех пользователей, кроме указанных
    @Query("DELETE FROM users WHERE id NOT IN (:userIds)")
    suspend fun deleteExcept(userIds: List<Int>)  // Добавлено

    // Очистить локальные данные (но оставить серверные ID)
    @Query("""
        UPDATE users SET 
            is_contact = 0,
            contact_nickname = NULL,
            is_favorite = 0,
            is_blocked = 0,
            sync_status = 'SYNCED'
    """)
    suspend fun clearLocalData()  // Добавлено

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}