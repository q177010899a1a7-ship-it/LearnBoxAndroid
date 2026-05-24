package com.learnbox.service

import com.google.gson.Gson
import com.learnbox.data.model.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LlmService {
    private val client = OkHttpClient.Builder().connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS).readTimeout(120, java.util.concurrent.TimeUnit.SECONDS).writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS).build()
    private val gson = Gson()

    data class ChatMsg(val role: String, val content: Any)
    data class ImageUrl(val url: String)
    data class ContentPart(val type: String, val text: String? = null, val image_url: ImageUrl? = null)
    data class ChatReq(val model: String, val messages: List<ChatMsg>, val max_tokens: Int, val temperature: Double)
    data class Choice(val message: ChatMsg)
    data class ChatResp(val choices: List<Choice>? = null)

    suspend fun chat(config: ApiConfig, messages: List<ChatMsg>): String = withContext(Dispatchers.IO) {
        val body = gson.toJson(ChatReq(config.model, messages, config.maxTokens, config.temperature))
        val req = Request.Builder()
            .url("${config.baseUrl}/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        val resp = client.newCall(req).execute()
        val json = resp.body?.string() ?: throw Exception("Empty response")
        android.util.Log.d("LlmService", "API Response: ${json.take(500)}")
        android.util.Log.d("LlmService", "Response code: ${resp.code}")
        val parsed = gson.fromJson(json, ChatResp::class.java)
        (parsed.choices ?: emptyList()).firstOrNull()?.message?.content?.toString() ?: throw Exception("No content")
    }

    suspend fun chatWithImages(config: ApiConfig, prompt: String, imagesBase64: List<String>): String {
        val contentParts = mutableListOf<ContentPart>()
        contentParts.add(ContentPart(type = "text", text = prompt))
        imagesBase64.forEach { b64 ->
            contentParts.add(ContentPart(type = "image_url", image_url = ImageUrl(url = "data:image/jpeg;base64,$b64")))
        }
        val messages = listOf(ChatMsg(role = "user", content = contentParts))
        val body = gson.toJson(ChatReq(config.model, messages, config.maxTokens, config.temperature))
        return withContext(Dispatchers.IO) {
            val req = Request.Builder()
                .url("${config.baseUrl}/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            val resp = client.newCall(req).execute()
            val json = resp.body?.string() ?: throw Exception("Empty response")
        android.util.Log.d("LlmService", "API Response: ${json.take(500)}")
        android.util.Log.d("LlmService", "Response code: ${resp.code}")
            val parsed = gson.fromJson(json, ChatResp::class.java)
            (parsed.choices ?: emptyList()).firstOrNull()?.message?.content?.toString() ?: throw Exception("No content")
        }
    }

    suspend fun parseNaturalDate(config: ApiConfig, text: String): String {
        val msgs = listOf(
            ChatMsg("system", "You are a date parsing assistant. Extract date and time from user input and return in format: YYYY-MM-DD HH:mm"),
            ChatMsg("user", text)
        )
        return chat(config, msgs).trim()
    }
}

