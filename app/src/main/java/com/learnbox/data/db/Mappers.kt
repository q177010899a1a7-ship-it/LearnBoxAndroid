package com.learnbox.data.db

import com.learnbox.data.model.*

fun VideoEntity.toModel() = Video(id,url,VideoPlatform.valueOf(platform),title,coverUrl,duration,WatchStatus.valueOf(status),tags,folderId,noteId,createdAt,watchedAt)
fun Video.toEntity() = VideoEntity(id,url,platform.name,title,coverUrl,duration,status.name,tags,folderId,noteId,createdAt,watchedAt)

fun NoteEntity.toModel() = Note(id,title,content,tags,videoId,template?.let{NoteTemplateType.valueOf(it)},summary,createdAt,updatedAt)
fun Note.toEntity() = NoteEntity(id,title,content,tags,videoId,template?.name,summary,createdAt,updatedAt)

fun ReminderEntity.toModel() = Reminder(id,content,remindAt,RepeatType.valueOf(repeatType),videoId,noteId,isCompleted,createdBy,createdAt)
fun Reminder.toEntity() = ReminderEntity(id,content,remindAt,repeatType.name,videoId,noteId,isCompleted,createdBy,createdAt)

fun ApiConfigEntity.toModel() = ApiConfig(id,name,baseUrl,apiKey,model,maxTokens,temperature,isDefault)
fun ApiConfig.toEntity() = ApiConfigEntity(id,name,baseUrl,apiKey,model,maxTokens,temperature,isDefault)
