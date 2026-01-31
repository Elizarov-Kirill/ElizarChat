// domain/model/SyncStatus.kt
package com.example.elizarchat.domain.model

/**
 * Статус синхронизации для всех сущностей
 */
enum class SyncStatus {
    SYNCED,         // Синхронизировано с сервером
    PENDING_SEND,   // Ожидает отправки
    PENDING_EDIT,   // Ожидает редактирования
    PENDING_DELETE, // Ожидает удаления
    PENDING,        // Общий статус ожидания
    DIRTY           // Локальные изменения
}