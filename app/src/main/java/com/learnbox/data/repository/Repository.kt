package com.learnbox.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.learnbox.data.db.AppDatabase
import com.learnbox.data.db.MindMapEntity
import com.learnbox.data.db.toEntity
import com.learnbox.data.db.toModel
import com.learnbox.data.model.*

class Repository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val videoDao = db.videoDao()
    private val noteDao = db.noteDao()
    private val reminderDao = db.reminderDao()
    private val apiConfigDao = db.apiConfigDao()
    private val mindMapDao = db.mindMapDao()
    private val gson = Gson()

    suspend fun getAllVideos(): List<Video> = videoDao.getAll().map { it.toModel() }
    suspend fun insertVideo(v: Video) = videoDao.insert(v.toEntity())
    suspend fun updateVideo(v: Video) = videoDao.update(v.toEntity())
    suspend fun deleteVideo(v: Video) = videoDao.delete(v.toEntity())
    suspend fun searchVideos(q: String) = videoDao.search(q).map { it.toModel() }

    suspend fun getAllNotes(): List<Note> = noteDao.getAll().map { it.toModel() }
    suspend fun insertNote(n: Note) = noteDao.insert(n.toEntity())
    suspend fun updateNote(n: Note) = noteDao.update(n.toEntity())
    suspend fun deleteNote(n: Note) = noteDao.delete(n.toEntity())
    suspend fun searchNotes(q: String) = noteDao.search(q).map { it.toModel() }

    suspend fun getAllReminders(): List<Reminder> = reminderDao.getAll().map { it.toModel() }
    suspend fun getPendingReminders(): List<Reminder> = reminderDao.getPending().map { it.toModel() }
    suspend fun insertReminder(r: Reminder) = reminderDao.insert(r.toEntity())
    suspend fun updateReminder(r: Reminder) = reminderDao.update(r.toEntity())
    suspend fun deleteReminder(r: Reminder) = reminderDao.delete(r.toEntity())
    suspend fun searchReminders(q: String) = reminderDao.search(q).map { it.toModel() }

    suspend fun getAllApiConfigs(): List<ApiConfig> = apiConfigDao.getAll().map { it.toModel() }
    suspend fun getDefaultApiConfig(): ApiConfig? = apiConfigDao.getDefault()?.toModel()
    suspend fun insertApiConfig(c: ApiConfig) {
        if (c.isDefault) { apiConfigDao.clearDefaults() }
        apiConfigDao.insert(c.toEntity())
    }
    suspend fun updateApiConfig(c: ApiConfig) {
        if (c.isDefault) { apiConfigDao.clearDefaults() }
        apiConfigDao.update(c.toEntity())
    }
    suspend fun deleteApiConfig(c: ApiConfig) = apiConfigDao.delete(c.toEntity())
    suspend fun setDefaultConfig(c: ApiConfig) {
        apiConfigDao.clearDefaults()
        apiConfigDao.update(c.copy(isDefault = true).toEntity())
    }

    // Mind Map operations
    suspend fun getAllMindMaps(): List<MindMap> {
        return mindMapDao.getAll().map { entity ->
            val nodes: List<MindNode> = try {
                val type = object : TypeToken<List<MindNode>>() {}.type
                gson.fromJson(entity.nodesJson, type) ?: emptyList()
            } catch (e: Exception) { emptyList() }
            MindMap(entity.id, entity.title, nodes, entity.createdAt, entity.updatedAt)
        }
    }

    suspend fun saveMindMap(mindMap: MindMap) {
        val nodesJson = gson.toJson(mindMap.nodes)
        mindMapDao.insert(MindMapEntity(mindMap.id, mindMap.title, nodesJson, mindMap.createdAt, mindMap.updatedAt))
    }

    suspend fun deleteMindMap(mindMap: MindMap) = mindMapDao.deleteById(mindMap.id)

    suspend fun searchAll(q: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        videoDao.search(q).forEach { results.add(SearchResult("video", it.title, "\u89c6\u9891: ${it.platform}")) }
        noteDao.search(q).forEach { results.add(SearchResult("note", it.title, "\u7b14\u8bb0: ${it.content.take(50)}")) }
        reminderDao.search(q).forEach { results.add(SearchResult("reminder", it.content, "\u63d0\u9192")) }
        return results
    }
}
