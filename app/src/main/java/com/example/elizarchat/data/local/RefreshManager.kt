package com.example.elizarchat.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class RefreshEvent {
    object RefreshChats : RefreshEvent()           // Обновить список чатов
    object RefreshMessages : RefreshEvent()        // Обновить сообщения в чате
    data class RefreshSpecificChat(val chatId: Int) : RefreshEvent()  // Обновить конкретный чат
}

object RefreshManager {
    private val _events = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<RefreshEvent> = _events.asSharedFlow()

    suspend fun notifyChatsChanged() {
        _events.emit(RefreshEvent.RefreshChats)
    }

    suspend fun notifyMessagesChanged() {
        _events.emit(RefreshEvent.RefreshMessages)
    }

    suspend fun notifyChatChanged(chatId: Int) {
        _events.emit(RefreshEvent.RefreshSpecificChat(chatId))
    }
}