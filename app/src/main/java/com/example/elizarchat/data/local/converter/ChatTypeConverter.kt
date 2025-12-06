package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import com.example.elizarchat.domain.model.ChatType

class ChatTypeConverter {
    @TypeConverter
    fun fromChatType(chatType: ChatType): String {
        return chatType.name
    }

    @TypeConverter
    fun toChatType(chatTypeString: String): ChatType {
        return ChatType.valueOf(chatTypeString)
    }
}