package com.learnbox.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "videos")
@TypeConverters(Converters::class)
data class VideoEntity(
    @PrimaryKey val id: String,
    val url: String,
    val platform: String,
    val title: String,
    val coverUrl: String?,
    val duration: Int?,
    val status: String,
    val tags: List<String>,
    val folderId: String?,
    val noteId: String?,
    val createdAt: Long,
    val watchedAt: Long?
)

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val tags: List<String>,
    val videoId: String?,
    val template: String?,
    val summary: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val content: String,
    val remindAt: Long,
    val repeatType: String,
    val videoId: String?,
    val noteId: String?,
    val isCompleted: Boolean,
    val createdBy: String,
    val createdAt: Long
)

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val maxTokens: Int,
    val temperature: Double,
    val isDefault: Boolean
)
