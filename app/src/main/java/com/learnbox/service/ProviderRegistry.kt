package com.learnbox.service

import android.content.Context
import android.content.SharedPreferences

data class AIProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val visionModels: List<String>,
    val textModels: List<String>,
    val description: String = ""
)

data class ModelStatus(
    val model: String,
    val providerId: String,
    var failCount: Int = 0,
    var lastFailTime: Long = 0,
    var totalCalls: Long = 0,
    var lastSuccessTime: Long = 0
) {
    val isCoolingDown: Boolean
        get() = failCount > 0 && (System.currentTimeMillis() - lastFailTime) < coolDownMs
    val coolDownMs: Long
        get() = when {
            failCount <= 1 -> 30_000L
            failCount <= 3 -> 120_000L
            else -> 600_000L
        }
    fun recordSuccess() { totalCalls++; lastSuccessTime = System.currentTimeMillis(); failCount = 0; lastFailTime = 0 }
    fun recordFailure() { failCount++; lastFailTime = System.currentTimeMillis() }
    fun reset() { failCount = 0; lastFailTime = 0 }
}

class ProviderRegistry(private val context: Context? = null) {
    companion object {
        private const val PREFS_NAME = "learnbox_providers"
        private const val KEY_ACTIVE_IDS = "active_ids"
        private const val KEY_PREFIX_KEY = "apikey_"

        val PRESETS = listOf(
            AIProvider("siliconflow", "SiliconFlow", "https://api.siliconflow.cn", "",
                listOf("Qwen/Qwen3-VL-8B-Instruct", "Qwen/Qwen3-VL-30B-A3B-Instruct", "Qwen/Qwen3-VL-32B-Instruct",
                       "Qwen/Qwen3-VL-8B-Thinking", "Qwen/Qwen3-VL-30B-A3B-Thinking", "Qwen/Qwen3-VL-32B-Thinking"),
                listOf("Qwen/Qwen3.5-4B", "Qwen/Qwen3.5-9B", "Qwen/Qwen3.6-27B", "Qwen/Qwen3.6-35B-A3B",
                       "Qwen/Qwen3-8B", "Qwen/Qwen3-14B", "Qwen/Qwen3-32B", "Qwen/Qwen2.5-72B-Instruct",
                       "deepseek-ai/DeepSeek-V4-Flash", "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B"),
                "\u56fd\u5185\u9996\u9009\uff0c\u514d\u8d39\u6a21\u578b\u6700\u591a"),
            AIProvider("groq", "Groq", "https://api.groq.com/openai", "",
                emptyList(),
                listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "gemma2-9b-it",
                       "mixtral-8x7b-32768", "llama3-70b-8192", "llama3-8b-8192"),
                "\u8d85\u5feb\u63a8\u7406\uff0c\u514d\u8d39\u6587\u672c\u6a21\u578b"),
            AIProvider("gemini", "Google Gemini", "https://generativelanguage.googleapis.com/v1beta/openai", "",
                listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-flash-8b"),
                listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-flash-8b", "gemini-1.5-pro"),
                "\u514d\u8d39\u989d\u5ea6\u5927\uff0c\u652f\u6301\u89c6\u89c9\u5206\u6790"),
            AIProvider("sambanova", "SambaNova", "https://api.sambanova.ai/v1", "",
                emptyList(),
                listOf("Meta-Llama-3.3-70B-Instruct", "DeepSeek-V3-0324", "QwQ-32B"),
                "\u9ad8\u901f\u63a8\u7406\uff0c\u514d\u8d39\u6587\u672c\u6a21\u578b")
        )
    }

    private val activeProviders = mutableListOf<AIProvider>()
    private val modelStatuses = mutableMapOf<String, ModelStatus>()

    init {
        loadSavedProviders()
    }

    private fun loadSavedProviders() {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val ids = prefs.getStringSet(KEY_ACTIVE_IDS, emptySet()) ?: emptySet()
        for (id in ids) {
            val key = prefs.getString(KEY_PREFIX_KEY + id, null) ?: continue
            val preset = PRESETS.firstOrNull { it.id == id } ?: continue
            activeProviders.add(preset.copy(apiKey = key))
        }
        refreshStatuses()
    }

    private fun saveProviders() {
        val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val editor = prefs.edit()
        val ids = activeProviders.map { it.id }.toSet()
        editor.putStringSet(KEY_ACTIVE_IDS, ids)
        for (p in activeProviders) {
            editor.putString(KEY_PREFIX_KEY + p.id, p.apiKey)
        }
        editor.apply()
    }

    private fun refreshStatuses() {
        for (p in activeProviders) {
            for (m in p.visionModels) modelStatuses.putIfAbsent("${p.id}::$m", ModelStatus(m, p.id))
            for (m in p.textModels) modelStatuses.putIfAbsent("${p.id}::$m", ModelStatus(m, p.id))
        }
    }

    fun addProvider(presetId: String, apiKey: String): Boolean {
        val preset = PRESETS.firstOrNull { it.id == presetId } ?: return false
        if (activeProviders.any { it.id == presetId }) return false
        activeProviders.add(preset.copy(apiKey = apiKey))
        refreshStatuses()
        saveProviders()
        return true
    }

    fun removeProvider(providerId: String) {
        activeProviders.removeAll { it.id == providerId }
        modelStatuses.keys.removeAll { it.startsWith("$providerId::") }
        saveProviders()
    }

    fun hasAnyProvider(): Boolean = activeProviders.isNotEmpty()

    fun getActiveProviders(): List<AIProvider> = activeProviders.toList()
    fun getAvailablePresets(): List<AIProvider> {
        val ids = activeProviders.map { it.id }.toSet()
        return PRESETS.filter { it.id !in ids }
    }

    fun getNextVisionModel(): Pair<AIProvider, String>? {
        val cands = mutableListOf<Triple<AIProvider, String, ModelStatus>>()
        for (p in activeProviders) for (m in p.visionModels) {
            val s = modelStatuses["${p.id}::$m"] ?: continue
            cands.add(Triple(p, m, s))
        }
        if (cands.isEmpty()) return null
        cands.sortWith(compareBy<Triple<AIProvider, String, ModelStatus>> { if (it.third.isCoolingDown) 1 else 0 }.thenBy { it.third.failCount }.thenBy { it.third.totalCalls })
        val best = cands.firstOrNull { !it.third.isCoolingDown } ?: cands.first()
        return Pair(best.first, best.second)
    }

    fun getNextTextModel(): Pair<AIProvider, String>? {
        val cands = mutableListOf<Triple<AIProvider, String, ModelStatus>>()
        for (p in activeProviders) for (m in p.textModels) {
            val s = modelStatuses["${p.id}::$m"] ?: continue
            cands.add(Triple(p, m, s))
        }
        if (cands.isEmpty()) return null
        cands.sortWith(compareBy<Triple<AIProvider, String, ModelStatus>> { if (it.third.isCoolingDown) 1 else 0 }.thenBy { it.third.failCount }.thenBy { it.third.totalCalls })
        val best = cands.firstOrNull { !it.third.isCoolingDown } ?: cands.first()
        return Pair(best.first, best.second)
    }

    fun recordSuccess(providerId: String, model: String) { modelStatuses["$providerId::$model"]?.recordSuccess() }
    fun recordFailure(providerId: String, model: String) { modelStatuses["$providerId::$model"]?.recordFailure() }
    fun resetAllCooldowns() { modelStatuses.values.forEach { it.reset() } }
    fun getModelStatuses(): Map<String, ModelStatus> = modelStatuses.toMap()
    fun getTotalModelCount(): Pair<Int, Int> {
        var v = 0; var t = 0
        for (p in activeProviders) { v += p.visionModels.size; t += p.textModels.size }
        return Pair(v, t)
    }
    fun isProviderActive(id: String) = activeProviders.any { it.id == id }
}
