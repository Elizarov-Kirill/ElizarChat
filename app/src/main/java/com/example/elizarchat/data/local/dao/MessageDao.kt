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
    suspend fun getMessageById(messageId: Int): MessageEntity?

    @Query("SELECT * FROM messages WHERE id = :messageId")
    fun observeMessageById(messageId: Int): Flow<MessageEntity?>

    // Сообщения чата с пагинацией (учитываем мягкое удаление)
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND deleted_at IS NULL
        ORDER BY created_at DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getMessagesByChat(chatId: Int, limit: Int, offset: Int): List<MessageEntity>

    // Все сообщения чата (для наблюдения)
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND deleted_at IS NULL
        ORDER BY created_at ASC
    """)
    fun observeMessagesByChat(chatId: Int): Flow<List<MessageEntity>>

    // Сообщения до определенного времени (для бесконечной прокрутки)
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND created_at < :before 
        AND deleted_at IS NULL
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun getMessagesBefore(chatId: Int, before: Long, limit: Int): List<MessageEntity>

    // Последнее сообщение чата
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND deleted_at IS NULL
        ORDER BY created_at DESC 
        LIMIT 1
    """)
    suspend fun getLastMessage(chatId: Int): MessageEntity?

    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND deleted_at IS NULL
        ORDER BY created_at DESC 
        LIMIT 1
    """)
    fun observeLastMessage(chatId: Int): Flow<MessageEntity?>

    // Сообщения со статусом "отправляется" (локальные)
    @Query("SELECT * FROM messages WHERE local_status = 'sending' OR local_status = 'error'")
    suspend fun getPendingMessages(): List<MessageEntity>

    // Количество сообщений в чате
    @Query("SELECT COUNT(*) FROM messages WHERE chat_id = :chatId AND deleted_at IS NULL")
    suspend fun getMessageCount(chatId: Int): Int

    // Количество непрочитанных сообщений для пользователя
    @Query("""
        SELECT COUNT(*) FROM messages m
        WHERE m.chat_id = :chatId 
        AND m.deleted_at IS NULL
        AND m.sender_id != :currentUserId
        AND NOT EXISTS (
            SELECT 1 FROM chat_members cm 
            WHERE cm.chat_id = m.chat_id 
            AND cm.user_id = :currentUserId 
            AND cm.last_read_message_id >= m.id
        )
    """)
    suspend fun getUnreadCount(chatId: Int, currentUserId: Int): Int

    // Поиск сообщений по тексту
    @Query("""
        SELECT * FROM messages 
        WHERE chat_id = :chatId 
        AND content LIKE '%' || :query || '%'
        AND deleted_at IS NULL
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun searchMessages(chatId: Int, query: String, limit: Int = 50): List<MessageEntity>

    // Сообщения, на которые еще нет ответа от сервера
    @Query("SELECT * FROM messages WHERE local_id IS NOT NULL AND sync_status IN ('PENDING_SEND', 'PENDING_EDIT', 'PENDING_DELETE')")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    // === UPDATE ===
    @Update
    suspend fun update(message: MessageEntity)

    // Обновление локального статуса сообщения
    @Query("UPDATE messages SET local_status = :status WHERE id = :messageId")
    suspend fun updateLocalStatus(messageId: Int, status: String)

    @Query("UPDATE messages SET local_status = :status WHERE local_id = :localId")
    suspend fun updateStatusByLocalId(localId: String, status: String)

    // Обновление серверного статуса сообщения
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateServerStatus(messageId: Int, status: String)

    // ВРЕМЕННО УДАЛЕНО: Слишком сложные запросы для SQLite
    // Проверка, прочитал ли пользователь сообщение
    // @Query("""
    //     SELECT EXISTS(
    //         SELECT 1 FROM messages
    //         WHERE id = :messageId
    //         AND read_by LIKE '%"' || :userId || '"%'
    //     )
    // """)
    // suspend fun isMessageReadByUser(messageId: Int, userId: Int): Boolean

    // ВРЕМЕННО УДАЛЕНО: Слишком сложные запросы для SQLite
    // Получить сообщения, которые не прочитаны пользователем
    // @Query("""
    //     SELECT * FROM messages
    //     WHERE chat_id = :chatId
    //     AND deleted_at IS NULL
    //     AND sender_id != :userId
    //     AND read_by NOT LIKE '%"' || :userId || '"%'
    //     ORDER BY created_at ASC
    // """)
    // suspend fun getUnreadMessages(chatId: Int, userId: Int): List<MessageEntity>

    // ВРЕМЕННО УДАЛЕНО: SQLite не поддерживает array_remove и операции с JSON массивами
    // Добавить пользователя в список прочитавших
    // @Query("""
    //     UPDATE messages
    //     SET read_by = read_by || :userId
    //     WHERE id = :messageId
    //     AND NOT (:userId IN read_by)
    // """)
    // suspend fun addUserToReadBy(messageId: Int, userId: Int)

    // ВРЕМЕННО УДАЛЕНО: SQLite не поддерживает array_remove
    // Удалить пользователя из списка прочитавших
    // @Query("""
    //     UPDATE messages
    //     SET read_by = array_remove(read_by, :userId)
    //     WHERE id = :messageId
    // """)
    // suspend fun removeUserFromReadBy(messageId: Int, userId: Int)

    // УПРОЩЕННАЯ ВЕРСИЯ: Обновить весь список прочитавших
    @Query("UPDATE messages SET read_by = :readBy WHERE id = :messageId")
    suspend fun updateReadBy(messageId: Int, readBy: List<Int>)

    // Обновление статуса синхронизации
    @Query("UPDATE messages SET sync_status = :syncStatus WHERE id = :messageId")
    suspend fun updateSyncStatus(messageId: Int, syncStatus: String)

    // Обновление после успешной отправки на сервер
    @Query("UPDATE messages SET id = :newId, local_id = NULL, status = 'sent', sync_status = 'SYNCED' WHERE local_id = :localId")
    suspend fun updateAfterServerSync(localId: String, newId: Int)

    // Пометить как отредактированное
    @Query("UPDATE messages SET content = :content, updated_at = :updatedAt WHERE id = :messageId")
    suspend fun markAsEdited(messageId: Int, content: String, updatedAt: Long)

    // Пометить как удаленное (мягкое удаление)
    @Query("UPDATE messages SET deleted_at = :deletedAt WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: Int, deletedAt: Long)

    @Query("UPDATE messages SET deleted_at = :deletedAt WHERE id IN (:messageIds)")
    suspend fun markAsDeleted(messageIds: List<Int>, deletedAt: Long)

    // Восстановить удаленное сообщение
    @Query("UPDATE messages SET deleted_at = NULL WHERE id = :messageId")
    suspend fun restoreMessage(messageId: Int)

    // === DELETE ===
    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteById(messageId: Int)

    // Удалить все сообщения чата
    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun deleteByChat(chatId: Int)

    // Удалить старые сообщения (очистка кэша)
    @Query("DELETE FROM messages WHERE created_at < :before")
    suspend fun deleteOlderThan(before: Long)

    // Удалить временные сообщения
    @Query("DELETE FROM messages WHERE local_id IS NOT NULL")
    suspend fun deleteTemporaryMessages()

    // Удалить мягко удаленные сообщения (полное удаление)
    @Query("DELETE FROM messages WHERE deleted_at IS NOT NULL")
    suspend fun deleteSoftDeletedMessages()

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    // === НОВЫЕ УПРОЩЕННЫЕ МЕТОДЫ ДЛЯ read_by ===

    /**
     * Проверяет, содержится ли userId в массиве read_by
     * Внимание: Медленный метод из-за LIKE, использовать осторожно
     */
    @Query("SELECT * FROM messages WHERE id = :messageId AND read_by LIKE '%' || :userId || '%'")
    suspend fun findIfUserReadMessage(messageId: Int, userId: Int): MessageEntity?

    /**
     * Очищает список прочитавших для сообщения
     */
    @Query("UPDATE messages SET read_by = '[]' WHERE id = :messageId")
    suspend fun clearReadBy(messageId: Int)

    /**
     * Устанавливает список прочитавших для нескольких сообщений
     */
    @Query("UPDATE messages SET read_by = :readBy WHERE id IN (:messageIds)")
    suspend fun updateReadByForMessages(messageIds: List<Int>, readBy: List<Int>)

    /**
     * Получает сообщения, где пользователь указан в read_by
     */
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND read_by LIKE '%' || :userId || '%' AND deleted_at IS NULL")
    suspend fun getMessagesReadByUser(chatId: Int, userId: Int): List<MessageEntity>
}