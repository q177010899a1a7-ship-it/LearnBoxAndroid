package com.learnbox.data.db

import androidx.room.*

@Dao
interface MindMapDao {
    @Query("SELECT * FROM mindmaps ORDER BY updatedAt DESC")
    suspend fun getAll(): List<MindMapEntity>

    @Query("SELECT * FROM mindmaps WHERE id = :id")
    suspend fun getById(id: String): MindMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mindMap: MindMapEntity)

    @Update
    suspend fun update(mindMap: MindMapEntity)

    @Delete
    suspend fun delete(mindMap: MindMapEntity)

    @Query("DELETE FROM mindmaps WHERE id = :id")
    suspend fun deleteById(id: String)
}