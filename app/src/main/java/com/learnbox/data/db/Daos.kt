package com.learnbox.data.db

import androidx.room.*

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    suspend fun getAll(): List<VideoEntity>
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getById(id: String): VideoEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VideoEntity)
    @Update
    suspend fun update(video: VideoEntity)
    @Delete
    suspend fun delete(video: VideoEntity)
    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun search(query: String): List<VideoEntity>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAll(): List<NoteEntity>
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)
    @Update
    suspend fun update(note: NoteEntity)
    @Delete
    suspend fun delete(note: NoteEntity)
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    suspend fun search(query: String): List<NoteEntity>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY remindAt ASC")
    suspend fun getAll(): List<ReminderEntity>
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY remindAt ASC")
    suspend fun getPending(): List<ReminderEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)
    @Update
    suspend fun update(reminder: ReminderEntity)
    @Delete
    suspend fun delete(reminder: ReminderEntity)
    @Query("SELECT * FROM reminders WHERE content LIKE '%' || :query || '%' ORDER BY remindAt ASC")
    suspend fun search(query: String): List<ReminderEntity>
}

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs")
    suspend fun getAll(): List<ApiConfigEntity>
    @Query("SELECT * FROM api_configs WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): ApiConfigEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ApiConfigEntity)
    @Update
    suspend fun update(config: ApiConfigEntity)
    @Delete
    suspend fun delete(config: ApiConfigEntity)
    @Query("UPDATE api_configs SET isDefault = 0")
    suspend fun clearDefaults()
}