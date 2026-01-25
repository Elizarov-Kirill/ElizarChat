package com.example.elizarchat.data.local.dao

import androidx.room.*
import com.example.elizarchat.data.local.entity.ChatMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {

    // === CREATE ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: ChatMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<ChatMemberEntity>)

    // === READ ===
    @Query("SELECT * FROM chat_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: Long): ChatMemberEntity?

    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun getMember(chatId: String, userId: String): ChatMemberEntity?

    // Все участники чата
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId ORDER BY joined_at ASC")
    suspend fun getMembersByChat(chatId: String): List<ChatMemberEntity>

    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId ORDER BY joined_at ASC")
    fun observeMembersByChat(chatId: String): Flow<List<ChatMemberEntity>>

    // Все чаты пользователя
    @Query("SELECT * FROM chat_members WHERE user_id = :userId ORDER BY joined_at DESC")
    suspend fun getChatsByUser(userId: String): List<ChatMemberEntity>

    @Query("SELECT * FROM chat_members WHERE user_id = :userId ORDER BY joined_at DESC")
    fun observeChatsByUser(userId: String): Flow<List<ChatMemberEntity>>

    // Участники с определенной ролью
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND role = :role")
    suspend fun getMembersByRole(chatId: String, role: String): List<ChatMemberEntity>

    // Количество участников
    @Query("SELECT COUNT(*) FROM chat_members WHERE chat_id = :chatId")
    suspend fun getMemberCount(chatId: String): Int

    // Проверка, является ли пользователь участником чата
    @Query("SELECT EXISTS(SELECT 1 FROM chat_members WHERE chat_id = :chatId AND user_id = :userId)")
    suspend fun isMember(chatId: String, userId: String): Boolean

    // Поиск участников по имени (через JOIN с users)
    @Query("""
        SELECT cm.* FROM chat_members cm
        INNER JOIN users u ON cm.user_id = u.id
        WHERE cm.chat_id = :chatId 
        AND (u.username LIKE '%' || :query || '%' OR u.display_name LIKE '%' || :query || '%')
        ORDER BY u.username
    """)
    suspend fun searchMembers(chatId: String, query: String): List<ChatMemberEntity>

    // === UPDATE ===
    @Update
    suspend fun update(member: ChatMemberEntity)

    // Обновление роли участника
    @Query("UPDATE chat_members SET role = :role, updated_at = :timestamp WHERE id = :memberId")
    suspend fun updateRole(memberId: Long, role: String, timestamp: Long)  // Instant → Long

    @Query("UPDATE chat_members SET role = :role, updated_at = :timestamp WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateMemberRole(chatId: String, userId: String, role: String, timestamp: Long)  // Instant → Long

    // Обновление времени последнего прочтения
    @Query("UPDATE chat_members SET last_read_message_id = :messageId, updated_at = :timestamp WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateLastReadMessage(chatId: String, userId: String, messageId: String?, timestamp: Long)  // Instant → Long

    // Обновление нескольких ролей
    @Query("UPDATE chat_members SET role = :role, updated_at = :timestamp WHERE chat_id = :chatId AND user_id IN (:userIds)")
    suspend fun updateMembersRole(chatId: String, userIds: List<String>, role: String, timestamp: Long)  // Instant → Long

    // === DELETE ===
    @Delete
    suspend fun delete(member: ChatMemberEntity)

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun deleteMember(chatId: String, userId: String)

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId")
    suspend fun deleteMembersByChat(chatId: String)

    @Query("DELETE FROM chat_members WHERE user_id = :userId")
    suspend fun deleteMembersByUser(userId: String)

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId AND role = :role")
    suspend fun deleteMembersByRole(chatId: String, role: String)

    @Query("DELETE FROM chat_members")
    suspend fun deleteAll()
}