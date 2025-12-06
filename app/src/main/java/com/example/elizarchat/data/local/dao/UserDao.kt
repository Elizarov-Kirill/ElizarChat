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

    // Контакты
    @Query("SELECT * FROM users WHERE isContact = 1 ORDER BY " +
            "CASE WHEN displayName IS NOT NULL THEN displayName ELSE username END")
    fun observeContacts(): Flow<List<UserEntity>>

    // Поиск
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR " +
            "displayName LIKE '%' || :query || '%' OR " +
            "email LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<UserEntity>

    // === UPDATE ===
    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateStatus(userId: String, isOnline: Boolean, lastSeen: Long?)

    // === DELETE ===
    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)
}