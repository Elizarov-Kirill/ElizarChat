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
    suspend fun getMemberById(memberId: Int): ChatMemberEntity?  // Изменено: Long → Int

    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun getMember(chatId: Int, userId: Int): ChatMemberEntity?  // Изменено: String → Int

    // Все участники чата
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId ORDER BY joined_at ASC")
    suspend fun getMembersByChat(chatId: Int): List<ChatMemberEntity>  // Изменено: String → Int

    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId ORDER BY joined_at ASC")
    fun observeMembersByChat(chatId: Int): Flow<List<ChatMemberEntity>>  // Изменено: String → Int

    // Все чаты пользователя
    @Query("SELECT * FROM chat_members WHERE user_id = :userId ORDER BY joined_at DESC")
    suspend fun getChatsByUser(userId: Int): List<ChatMemberEntity>  // Изменено: String → Int

    @Query("SELECT * FROM chat_members WHERE user_id = :userId ORDER BY joined_at DESC")
    fun observeChatsByUser(userId: Int): Flow<List<ChatMemberEntity>>  // Изменено: String → Int

    // Участники с определенной ролью
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND role = :role")
    suspend fun getMembersByRole(chatId: Int, role: String): List<ChatMemberEntity>  // Изменено: String → Int

    // Участники с непрочитанными сообщениями
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND unread_count > 0")
    suspend fun getMembersWithUnread(chatId: Int): List<ChatMemberEntity>  // Добавлено

    // Количество участников
    @Query("SELECT COUNT(*) FROM chat_members WHERE chat_id = :chatId")
    suspend fun getMemberCount(chatId: Int): Int  // Изменено: String → Int

    // Количество непрочитанных сообщений пользователя в чате
    @Query("SELECT unread_count FROM chat_members WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun getUnreadCount(chatId: Int, userId: Int): Int?  // Добавлено

    // Проверка, является ли пользователь участником чата
    @Query("SELECT EXISTS(SELECT 1 FROM chat_members WHERE chat_id = :chatId AND user_id = :userId)")
    suspend fun isMember(chatId: Int, userId: Int): Boolean  // Изменено: String → Int

    // Поиск участников по имени (через JOIN с users)
    @Query("""
        SELECT cm.* FROM chat_members cm
        INNER JOIN users u ON cm.user_id = u.id
        WHERE cm.chat_id = :chatId 
        AND (u.username LIKE '%' || :query || '%' OR u.display_name LIKE '%' || :query || '%')
        ORDER BY u.username
    """)
    suspend fun searchMembers(chatId: Int, query: String): List<ChatMemberEntity>  // Изменено: String → Int

    // Получить несколько участников по ID пользователей
    @Query("SELECT * FROM chat_members WHERE chat_id = :chatId AND user_id IN (:userIds)")
    suspend fun getMembersByUserIds(chatId: Int, userIds: List<Int>): List<ChatMemberEntity>  // Добавлено

    // === UPDATE ===
    @Update
    suspend fun update(member: ChatMemberEntity)

    // Обновление роли участника
    @Query("UPDATE chat_members SET role = :role WHERE id = :memberId")
    suspend fun updateRole(memberId: Int, role: String)  // Изменено: Long → Int, убрал timestamp

    @Query("UPDATE chat_members SET role = :role WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateMemberRole(chatId: Int, userId: Int, role: String)  // Изменено: String → Int

    // Обновление времени последнего прочтения
    @Query("UPDATE chat_members SET last_read_message_id = :messageId WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateLastReadMessage(chatId: Int, userId: Int, messageId: Int?)  // Изменено: String → Int

    // Обновление счетчика непрочитанных
    @Query("UPDATE chat_members SET unread_count = :count WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateUnreadCount(chatId: Int, userId: Int, count: Int)  // Добавлено

    // Увеличить счетчик непрочитанных
    @Query("UPDATE chat_members SET unread_count = unread_count + 1 WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun incrementUnreadCount(chatId: Int, userId: Int)  // Добавлено

    // Уменьшить счетчик непрочитанных
    @Query("UPDATE chat_members SET unread_count = unread_count - :amount WHERE chat_id = :chatId AND user_id = :userId AND unread_count >= :amount")
    suspend fun decrementUnreadCount(chatId: Int, userId: Int, amount: Int = 1)  // Добавлено

    // Сбросить счетчик непрочитанных
    @Query("UPDATE chat_members SET unread_count = 0 WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun resetUnreadCount(chatId: Int, userId: Int)  // Добавлено

    // Обновление уведомлений
    @Query("UPDATE chat_members SET notifications_enabled = :enabled WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateNotifications(chatId: Int, userId: Int, enabled: Boolean)  // Добавлено

    // Обновление статуса скрытия
    @Query("UPDATE chat_members SET is_hidden = :hidden WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun updateHiddenStatus(chatId: Int, userId: Int, hidden: Boolean)  // Добавлено

    // Обновление статуса синхронизации
    @Query("UPDATE chat_members SET sync_status = :syncStatus, last_sync_at = :timestamp WHERE id = :memberId")
    suspend fun updateSyncStatus(memberId: Int, syncStatus: String, timestamp: Long = System.currentTimeMillis())  // Добавлено

    // Обновление нескольких ролей
    @Query("UPDATE chat_members SET role = :role WHERE chat_id = :chatId AND user_id IN (:userIds)")
    suspend fun updateMembersRole(chatId: Int, userIds: List<Int>, role: String)  // Изменено: String → Int

    // === DELETE ===
    @Delete
    suspend fun delete(member: ChatMemberEntity)

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId AND user_id = :userId")
    suspend fun deleteMember(chatId: Int, userId: Int)  // Изменено: String → Int

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId")
    suspend fun deleteMembersByChat(chatId: Int)  // Изменено: String → Int

    @Query("DELETE FROM chat_members WHERE user_id = :userId")
    suspend fun deleteMembersByUser(userId: Int)  // Изменено: String → Int

    @Query("DELETE FROM chat_members WHERE chat_id = :chatId AND role = :role")
    suspend fun deleteMembersByRole(chatId: Int, role: String)  // Изменено: String → Int

    // Удалить нескольких участников
    @Query("DELETE FROM chat_members WHERE chat_id = :chatId AND user_id IN (:userIds)")
    suspend fun deleteMembers(chatId: Int, userIds: List<Int>)  // Добавлено

    @Query("DELETE FROM chat_members")
    suspend fun deleteAll()
}