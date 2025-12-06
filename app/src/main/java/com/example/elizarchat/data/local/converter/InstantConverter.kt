package com.example.elizarchat.data.local.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Конвертер для java.time.Instant.
 * Room будет хранить Instant как Long (миллисекунды с эпохи Unix).
 */
class InstantConverter {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
}

/**
 * Дополнительный конвертер для строк из API (опционально)
 */
class StringToInstantConverter {
    @TypeConverter
    fun fromString(value: String?): Instant? {
        return try {
            value?.let { Instant.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    @TypeConverter
    fun toString(value: Instant?): String? {
        return value?.toString()
    }
}