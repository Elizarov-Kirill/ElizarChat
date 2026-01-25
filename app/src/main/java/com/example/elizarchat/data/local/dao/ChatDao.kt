package com.example.elizarchat.data.local.dao

import androidx.room.*
import com.example.elizarchat.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // === CREATE ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    // === READ ===
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: String): Flow<ChatEntity?>

    // Все чаты с сортировкой по обновлению
    @Query("""
        SELECT * FROM chats 
        WHERE is_deleted = false
        ORDER BY 
            CASE 
                WHEN updated_at IS NOT NULL THEN updated_at 
                ELSE created_at 
            END DESC
    """)
    fun observeAllChats(): Flow<List<ChatEntity>>

    // Чаты определенного типа
    @Query("SELECT * FROM chats WHERE type = :type AND is_deleted = false ORDER BY updated_at DESC")
    fun observeChatsByType(type: String): Flow<List<ChatEntity>>

    // Приватный чат между двумя пользователями
    @Query("""
        SELECT c.* FROM chats c
        INNER JOIN chat_members cm1 ON c.id = cm1.chat_id
        INNER JOIN chat_members cm2 ON c.id = cm2.chat_id
        WHERE c.type = 'private'
        AND cm1.user_id = :userId1
        AND cm2.user_id = :userId2
        AND (SELECT COUNT(*) FROM chat_members WHERE chat_id = c.id) = 2
        LIMIT 1
    """)
    suspend fun getPrivateChatBetween(userId1: String, userId2: String): ChatEntity?

    // Поиск чатов
    @Query("""
        SELECT * FROM chats 
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND is_deleted = false
        ORDER BY updated_at DESC
    """)
    suspend fun searchChats(query: String): List<ChatEntity>

    // Непрочитанные чаты
    @Query("SELECT * FROM chats WHERE unread_count > 0 AND is_deleted = false")
    fun observeUnreadChats(): Flow<List<ChatEntity>>

    // Избранные чаты
    @Query("SELECT * FROM chats WHERE is_pinned = true AND is_deleted = false ORDER BY updated_at DESC")
    fun observePinnedChats(): Flow<List<ChatEntity>>

    // Чат по последнему сообщению
    @Query("SELECT * FROM chats WHERE last_message_id = :messageId")
    suspend fun getChatByLastMessage(messageId: String): ChatEntity?

    // === UPDATE ===
    @Update
    suspend fun update(chat: ChatEntity)

    @Query("UPDATE chats SET updated_at = :timestamp WHERE id = :chatId")
    suspend fun updateTimestamp(chatId: String, timestamp: Long)  // Instant → Long

    @Query("UPDATE chats SET last_message_id = :messageId, updated_at = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, messageId: String, timestamp: Long)  // Instant → Long

    @Query("UPDATE chats SET unread_count = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: String, count: Int)

    @Query("UPDATE chats SET is_muted = :muted WHERE id = :chatId")
    suspend fun updateMutedStatus(chatId: String, muted: Boolean)

    @Query("UPDATE chats SET is_pinned = :pinned WHERE id = :chatId")
    suspend fun updatePinnedStatus(chatId: String, pinned: Boolean)

    @Query("UPDATE chats SET name = :name, description = :description, updated_at = :timestamp WHERE id = :chatId")
    suspend fun updateChatInfo(chatId: String, name: String?, description: String?, timestamp: Long)  // Instant → Long

    // Увеличить счетчик непрочитанных
    @Query("UPDATE chats SET unread_count = unread_count + 1, updated_at = :timestamp WHERE id = :chatId")
    suspend fun incrementUnreadCount(chatId: String, timestamp: Long)  // Instant → Long

    // Сбросить счетчик непрочитанных
    @Query("UPDATE chats SET unread_count = 0 WHERE id = :chatId")
    suspend fun resetUnreadCount(chatId: String)

    // === DELETE ===
    @Delete
    suspend fun delete(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteById(chatId: String)

    // Мягкое удаление
    @Query("UPDATE chats SET is_deleted = true, updated_at = :timestamp WHERE id = :chatId")
    suspend fun markAsDeleted(chatId: String, timestamp: Long = System.currentTimeMillis())  // Instant → Long

    @Query("DELETE FROM chats")
    suspend fun deleteAll()
}