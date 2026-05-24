package com.learnbox.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class, NoteEntity::class, ReminderEntity::class, ApiConfigEntity::class, MindMapEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun mindMapDao(): MindMapDao
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "learnbox.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
