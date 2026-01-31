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
    suspend fun getChatById(chatId: Int): ChatEntity?  // Изменено: String → Int

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: Int): Flow<ChatEntity?>  // Изменено: String → Int

    // Все чаты с сортировкой по последней активности
    @Query("""
        SELECT * FROM chats 
        ORDER BY 
            CASE 
                WHEN last_message_at IS NOT NULL THEN last_message_at 
                ELSE created_at 
            END DESC
    """)
    fun observeAllChats(): Flow<List<ChatEntity>>

    // Чаты определенного типа
    @Query("SELECT * FROM chats WHERE type = :type ORDER BY last_message_at DESC NULLS LAST")
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
    suspend fun getPrivateChatBetween(userId1: Int, userId2: Int): ChatEntity?  // Изменено: String → Int

    // Поиск чатов по названию
    @Query("""
        SELECT * FROM chats 
        WHERE name LIKE '%' || :query || '%'
        ORDER BY last_message_at DESC NULLS LAST
        LIMIT :limit
    """)
    suspend fun searchChats(query: String, limit: Int = 20): List<ChatEntity>

    // Приватные чаты с пользователем
    @Query("""
        SELECT c.* FROM chats c
        INNER JOIN chat_members cm ON c.id = cm.chat_id
        WHERE c.type = 'private'
        AND cm.user_id = :userId
        ORDER BY c.last_message_at DESC NULLS LAST
    """)
    suspend fun getPrivateChatsWithUser(userId: Int): List<ChatEntity>  // Добавлено

    // Групповые чаты
    @Query("SELECT * FROM chats WHERE type IN ('group', 'channel') ORDER BY last_message_at DESC NULLS LAST")
    fun observeGroupChats(): Flow<List<ChatEntity>>  // Добавлено

    // Чаты, созданные пользователем
    @Query("SELECT * FROM chats WHERE created_by = :userId ORDER BY created_at DESC")
    suspend fun getChatsCreatedBy(userId: Int): List<ChatEntity>  // Добавлено

    // Чаты по последнему сообщению (по timestamp)
    @Query("SELECT * FROM chats WHERE last_message_at = :timestamp")
    suspend fun getChatByLastMessageTimestamp(timestamp: Long): ChatEntity?  // Добавлено

    // Чаты, которые нужно синхронизировать
    @Query("SELECT * FROM chats WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedChats(): List<ChatEntity>  // Добавлено

    // === UPDATE ===
    @Update
    suspend fun update(chat: ChatEntity)

    // Обновление времени последнего сообщения
    @Query("UPDATE chats SET last_message_at = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessageAt(chatId: Int, timestamp: Long)  // Добавлено, String → Int

    // Обновление локального ID последнего сообщения
    @Query("UPDATE chats SET last_message_id = :messageId WHERE id = :chatId")
    suspend fun updateLastMessageId(chatId: Int, messageId: Int?)  // Изменено: String → Int

    @Query("UPDATE chats SET updated_at = :timestamp WHERE id = :chatId")
    suspend fun updateTimestamp(chatId: Int, timestamp: Long)  // Изменено: String → Int

    @Query("UPDATE chats SET is_muted = :muted WHERE id = :chatId")
    suspend fun updateMutedStatus(chatId: Int, muted: Boolean)  // Изменено: String → Int

    @Query("UPDATE chats SET is_pinned = :pinned WHERE id = :chatId")
    suspend fun updatePinnedStatus(chatId: Int, pinned: Boolean)  // Изменено: String → Int

    @Query("UPDATE chats SET name = :name, avatar_url = :avatarUrl, updated_at = :timestamp WHERE id = :chatId")
    suspend fun updateChatInfo(chatId: Int, name: String?, avatarUrl: String?, timestamp: Long)  // Изменено

    // Обновление статуса синхронизации
    @Query("UPDATE chats SET sync_status = :syncStatus, last_sync_at = :timestamp WHERE id = :chatId")
    suspend fun updateSyncStatus(chatId: Int, syncStatus: String, timestamp: Long = System.currentTimeMillis())  // Добавлено

    // Обновление типа чата
    @Query("UPDATE chats SET type = :type WHERE id = :chatId")
    suspend fun updateChatType(chatId: Int, type: String)  // Добавлено

    // Пометка как синхронизированного
    @Query("UPDATE chats SET sync_status = 'SYNCED', last_sync_at = :timestamp WHERE id = :chatId")
    suspend fun markAsSynced(chatId: Int, timestamp: Long = System.currentTimeMillis())  // Добавлено

    // === DELETE ===
    @Delete
    suspend fun delete(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteById(chatId: Int)  // Изменено: String → Int

    // Удалить все чаты
    @Query("DELETE FROM chats")
    suspend fun deleteAll()

    // Удалить чаты, кроме указанных
    @Query("DELETE FROM chats WHERE id NOT IN (:chatIds)")
    suspend fun deleteExcept(chatIds: List<Int>)  // Добавлено

    // Очистить локальные данные (но оставить серверные ID)
    @Query("""
        UPDATE chats SET 
            last_message_id = NULL,
            is_muted = false,
            is_pinned = false,
            sync_status = 'SYNCED'
    """)
    suspend fun clearLocalData()  // Добавлено
}