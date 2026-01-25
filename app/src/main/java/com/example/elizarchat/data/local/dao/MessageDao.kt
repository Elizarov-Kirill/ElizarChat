package com.example.elizarchat.data.local.dao

import androidx.room.*
import com.example.elizarchat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // === CREATE ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    // === READ ===
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :messageId")
    fun observeMessageById(messageId: String): Flow<MessageEntity?>

    // Сообщения чата с пагинацией
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND is_deleted = false
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getMessagesByChat(chatId: String, limit: Int, offset: Int): List<MessageEntity>

    // Все сообщения чата (для наблюдения)
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND is_deleted = false
        ORDER BY created_at ASC
    """)
    fun observeMessagesByChat(chatId: String): Flow<List<MessageEntity>>

    // Сообщения до определенного времени (для бесконечной прокрутки)
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND created_at < :before 
        AND is_deleted = false
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun getMessagesBefore(chatId: String, before: Long, limit: Int): List<MessageEntity>  // Instant → Long

    // Последнее сообщение чата
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND is_deleted = false
        ORDER BY created_at DESC 
        LIMIT 1
    """)
    suspend fun getLastMessage(chatId: String): MessageEntity?

    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND is_deleted = false
        ORDER BY created_at DESC 
        LIMIT 1
    """)
    fun observeLastMessage(chatId: String): Flow<MessageEntity?>

    // Сообщения со статусом "отправляется"
    @Query("SELECT * FROM messages WHERE status = 'sending' OR status = 'error'")
    suspend fun getPendingMessages(): List<MessageEntity>

    // Количество сообщений в чате
    @Query("SELECT COUNT(*) FROM messages WHERE chat_id = :chatId AND is_deleted = false")
    suspend fun getMessageCount(chatId: String): Int

    // Количество непрочитанных сообщений
    @Query("""
        SELECT COUNT(*) FROM messages m
        WHERE m.chat_id = :chatId 
        AND m.is_deleted = false
        AND m.user_id != :currentUserId
        AND NOT EXISTS (
            SELECT 1 FROM chat_members cm 
            WHERE cm.chat_id = m.chat_id 
            AND cm.user_id = :currentUserId 
            AND cm.last_read_message_id >= m.id
        )
    """)
    suspend fun getUnreadCount(chatId: String, currentUserId: String): Int

    // Поиск сообщений по тексту
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND content LIKE '%' || :query || '%'
        AND is_deleted = false
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun searchMessages(chatId: String, query: String, limit: Int = 50): List<MessageEntity>

    // Сообщения, на которые еще нет ответа от сервера
    @Query("SELECT * FROM messages WHERE local_id IS NOT NULL AND sync_status IN ('PENDING_SEND', 'PENDING_EDIT', 'PENDING_DELETE')")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    // === UPDATE ===
    @Update
    suspend fun update(message: MessageEntity)

    // Обновление статуса сообщения
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: String)

    @Query("UPDATE messages SET status = :status WHERE local_id = :localId")
    suspend fun updateStatusByLocalId(localId: String, status: String)

    // Обновление нескольких статусов
    @Query("UPDATE messages SET status = :status WHERE id IN (:messageIds)")
    suspend fun updateStatuses(messageIds: List<String>, status: String)

    // Обновление статуса синхронизации
    @Query("UPDATE messages SET sync_status = :syncStatus WHERE id = :messageId")
    suspend fun updateSyncStatus(messageId: String, syncStatus: String)

    // Обновление после успешной отправки на сервер
    @Query("UPDATE messages SET id = :newId, local_id = NULL, status = 'sent', sync_status = 'SYNCED' WHERE local_id = :localId")
    suspend fun updateAfterServerSync(localId: String, newId: String)

    // Пометить как отредактированное
    @Query("UPDATE messages SET content = :content, is_edited = true, updated_at = :updatedAt WHERE id = :messageId")
    suspend fun markAsEdited(messageId: String, content: String, updatedAt: Long)  // Instant → Long

    // Пометить как удаленное
    @Query("UPDATE messages SET is_deleted = true, updated_at = :updatedAt WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: String, updatedAt: Long)  // Instant → Long

    @Query("UPDATE messages SET is_deleted = true, updated_at = :updatedAt WHERE id IN (:messageIds)")
    suspend fun markAsDeleted(messageIds: List<String>, updatedAt: Long)  // Instant → Long

    // === DELETE ===
    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteById(messageId: String)

    // Удалить все сообщения чата
    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun deleteByChat(chatId: String)

    // Удалить старые сообщения (очистка кэша)
    @Query("DELETE FROM messages WHERE created_at < :before")
    suspend fun deleteOlderThan(before: Long)  // Instant → Long

    // Удалить временные сообщения
    @Query("DELETE FROM messages WHERE local_id IS NOT NULL")
    suspend fun deleteTemporaryMessages()

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}