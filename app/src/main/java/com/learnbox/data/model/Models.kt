package com.learnbox.data.model

import java.util.UUID

enum class VideoPlatform(val label: String) {
    DOUYIN("抖音"), BILIBILI("B站"), XIAOHONGSHU("小红书"), YOUTUBE("YouTube"), OTHER("其他")
}

enum class WatchStatus(val label: String) {
    UNWATCHED("未看"), WATCHING("观看中"), WATCHED("已看完")
}

enum class NoteTemplateType(val label: String) {
    CORNELL("康奈尔笔记"), OUTLINE("大纲笔记"), MIND_MAP("思维导图"),
    FLOW("流程笔记"), QA("问答笔记"), VIDEO_SUMMARY("视频摘要"), DAILY_NOTE("每日笔记")
}

enum class RepeatType(val label: String) {
    NONE("不重复"), DAILY("每天"), WEEKLY("每周"), MONTHLY("每月"), WEEKDAYS("工作日")
}

data class Video(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val platform: VideoPlatform = VideoPlatform.DOUYIN,
    val title: String,
    val coverUrl: String? = null,
    val duration: Int? = null,
    val status: WatchStatus = WatchStatus.UNWATCHED,
    val tags: List<String> = emptyList(),
    val folderId: String? = null,
    val noteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val watchedAt: Long? = null
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val videoId: String? = null,
    val template: NoteTemplateType? = null,
    val summary: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val remindAt: Long,
    val repeatType: RepeatType = RepeatType.NONE,
    val videoId: String? = null,
    val noteId: String? = null,
    val isCompleted: Boolean = false,
    val createdBy: String = "user",
    val createdAt: Long = System.currentTimeMillis()
)

data class ApiConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val maxTokens: Int = 1000,
    val temperature: Double = 0.7,
    val isDefault: Boolean = false
)

data class SearchResult(
    val type: String,
    val title: String,
    val snippet: String
)
