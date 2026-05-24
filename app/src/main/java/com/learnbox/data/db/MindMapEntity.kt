package com.learnbox.data.db

import androidx.room.*

@Entity(tableName = "mindmaps")
@TypeConverters(Converters::class)
data class MindMapEntity(
    @PrimaryKey val id: String,
    val title: String,
    val nodesJson: String,
    val createdAt: Long,
    val updatedAt: Long
)