package com.example.elizarchat.data.local.dao

import androidx.room.*
import com.example.elizarchat.data.local.entity.ChatEntity
import com.example.elizarchat.data.local.entity.ChatParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // === ChatEntity операции ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: String): Flow<ChatEntity?>

    // Все чаты с сортировкой по последнему сообщению
    @Query("SELECT * FROM chats ORDER BY " +
            "CASE WHEN lastMessageAt IS NULL THEN createdAt ELSE lastMessageAt END DESC")
    fun observeAllChats(): Flow<List<ChatEntity>>

    // Чаты, где пользователь является участником
    @Query("""
        SELECT c.* FROM chats c
        INNER JOIN chat_participants cp ON c.id = cp.chatId
        WHERE cp.userId = :userId
        ORDER BY 
        CASE WHEN c.lastMessageAt IS NULL THEN c.createdAt ELSE c.lastMessageAt END DESC
    """)
    fun observeChatsForUser(userId: String): Flow<List<ChatEntity>>

    // Поиск чатов по названию
    @Query("SELECT * FROM chats WHERE name LIKE '%' || :query || '%'")
    suspend fun searchChats(query: String): List<ChatEntity>

    @Update
    suspend fun update(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessageAt = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessageTime(chatId: String, timestamp: Long?)

    @Query("UPDATE chats SET unreadCount = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: String, count: Int)

    @Delete
    suspend fun delete(chat: ChatEntity)

    // === ChatParticipantEntity операции ===

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ChatParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParticipants(participants: List<ChatParticipantEntity>)

    @Query("SELECT * FROM chat_participants WHERE chatId = :chatId")
    suspend fun getParticipantsForChat(chatId: String): List<ChatParticipantEntity>

    @Query("SELECT * FROM chat_participants WHERE userId = :userId")
    suspend fun getChatsForUser(userId: String): List<ChatParticipantEntity>

    @Query("DELETE FROM chat_participants WHERE chatId = :chatId AND userId = :userId")
    suspend fun removeParticipant(chatId: String, userId: String)

    @Query("DELETE FROM chat_participants WHERE chatId = :chatId")
    suspend fun removeAllParticipants(chatId: String)

    // Полная очистка
    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    @Query("DELETE FROM chat_participants")
    suspend fun deleteAllParticipants()
}