package com.learnbox.service

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class VideoAnalyzer(private val context: Context) {

    companion object {
        private const val TAG = "VideoAnalyzer"
    }

    data class VideoFrame(val timestampMs: Long, val base64: String)

    suspend fun extractFrames(videoUri: Uri, intervalMs: Long = 15000L, maxFrames: Int = 5): List<VideoFrame> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val frames = mutableListOf<VideoFrame>()
        var tempFile: File? = null
        try {
            val uriStr = videoUri.toString()
            Log.d(TAG, "URI: $uriStr")
            if (uriStr.startsWith("http://") || uriStr.startsWith("https://")) {
                Log.d(TAG, "Downloading...")
                tempFile = File(context.cacheDir, "video_temp.mp4")
                downloadToFile(uriStr, tempFile!!)
                retriever.setDataSource(tempFile!!.absolutePath)
            } else {
                retriever.setDataSource(context, videoUri)
            }
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            Log.d(TAG, "Duration: ${durationMs}ms")
            if (durationMs <= 0) {
                val bmp = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (bmp != null) {
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    frames.add(VideoFrame(0, Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)))
                    bmp.recycle()
                }
                return@withContext frames
            }
            var t = 0L
            while (t < durationMs && frames.size < maxFrames) {
                try {
                    val bmp = retriever.getFrameAtTime(t * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    if (bmp != null) {
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                        frames.add(VideoFrame(t, Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)))
                        bmp.recycle()
                    }
                } catch (_: Exception) {}
                t += intervalMs
            }
            Log.d(TAG, "Frames: ${frames.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
        } finally {
            try { retriever.release() } catch (_: Exception) {}
            tempFile?.let { try { it.delete() } catch (_: Exception) {} }
        }
        frames
    }

    private fun downloadToFile(urlStr: String, file: File) {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        conn.connectTimeout = 30000
        conn.readTimeout = 120000
        conn.instanceFollowRedirects = true
        conn.connect()
        if (conn.responseCode != 200) throw Exception("HTTP ${conn.responseCode}")
        conn.inputStream.use { input ->
            file.outputStream().use { output ->
                val buf = ByteArray(8192)
                var n: Int
                while (input.read(buf).also { n = it } != -1) output.write(buf, 0, n)
            }
        }
        Log.d(TAG, "Downloaded ${file.length()} bytes")
    }

    fun buildAnalysisPrompt(videoTitle: String): String {
        return "你是一个专业的学习助手，请用中文分析以下视频内容。视频标题：" + videoTitle + "\n\n" +
            "请严格按照以下格式输出，每部分用对应标题开头：\n\n" +
            "【视频摘要】\n" +
            "用2-3句话概括视频的核心内容。\n\n" +
            "【关键要点】\n" +
            "列出3-6个关键要点，每个要点一行，用数字编号。\n\n" +
            "【详细笔记】\n" +
            "将视频内容整理成结构化的学习笔记，使用层级格式。\n\n" +
            "【思维导图】\n" +
            "用文本树形结构展示思维导图大纲。\n\n" +
            "【学习建议】\n" +
            "根据内容给出1-2条学习建议或思考问题。\n\n" +
            "注意：全部使用中文回答，内容要准确、简洁、有条理。"
    }
}
