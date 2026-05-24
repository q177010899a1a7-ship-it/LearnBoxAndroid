package com.learnbox.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.learnbox.data.model.*
import com.learnbox.data.repository.Repository
import com.learnbox.service.LlmService
import com.learnbox.service.ModelPoolManager
import com.learnbox.service.VideoAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)
    private val llm = LlmService()
    val modelPool = ModelPoolManager(app)

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()
    private val _apiConfigs = MutableStateFlow<List<ApiConfig>>(emptyList())
    val apiConfigs: StateFlow<List<ApiConfig>> = _apiConfigs.asStateFlow()
    private val _defaultConfig = MutableStateFlow<ApiConfig?>(null)
    val defaultConfig: StateFlow<ApiConfig?> = _defaultConfig.asStateFlow()
    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()
    private val _mindMaps = MutableStateFlow<List<MindMap>>(emptyList())
    val mindMaps: StateFlow<List<MindMap>> = _mindMaps.asStateFlow()
    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult: StateFlow<String?> = _analysisResult.asStateFlow()
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    private val _poolStatus = MutableStateFlow("")
    val poolStatus: StateFlow<String> = _poolStatus.asStateFlow()
    private val _hasProvider = MutableStateFlow(false)
    val hasProvider: StateFlow<Boolean> = _hasProvider.asStateFlow()

    init {
        loadAll()
        _poolStatus.value = modelPool.getStatusSummary()
        _hasProvider.value = modelPool.registry.hasAnyProvider()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _videos.value = repo.getAllVideos()
            _notes.value = repo.getAllNotes()
            _reminders.value = repo.getAllReminders()
            _apiConfigs.value = repo.getAllApiConfigs()
            _defaultConfig.value = repo.getDefaultApiConfig()
            _mindMaps.value = repo.getAllMindMaps()
        }
    }

    fun addVideo(url: String, platform: VideoPlatform, title: String) {
        viewModelScope.launch { repo.insertVideo(Video(url = url, platform = platform, title = title)); _videos.value = repo.getAllVideos() }
    }
    fun changeVideoStatus(video: Video, status: WatchStatus) {
        viewModelScope.launch { repo.updateVideo(video.copy(status = status, watchedAt = if (status == WatchStatus.WATCHED) System.currentTimeMillis() else video.watchedAt)); _videos.value = repo.getAllVideos() }
    }
    fun deleteVideo(video: Video) {
        viewModelScope.launch { repo.deleteVideo(video); _videos.value = repo.getAllVideos() }
    }
    fun addNote(title: String, content: String, template: NoteTemplateType?) {
        viewModelScope.launch { repo.insertNote(Note(title = title, content = content, template = template)); _notes.value = repo.getAllNotes() }
    }
    fun updateNote(note: Note) {
        viewModelScope.launch { repo.updateNote(note); _notes.value = repo.getAllNotes() }
    }
    fun deleteNote(note: Note) {
        viewModelScope.launch { repo.deleteNote(note); _notes.value = repo.getAllNotes() }
    }
    fun addReminder(content: String, remindAt: Long, repeatType: RepeatType) {
        viewModelScope.launch { repo.insertReminder(Reminder(content = content, remindAt = remindAt, repeatType = repeatType)); _reminders.value = repo.getAllReminders() }
    }
    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch { repo.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted)); _reminders.value = repo.getAllReminders() }
    }
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { repo.deleteReminder(reminder); _reminders.value = repo.getAllReminders() }
    }
    fun search(query: String) {
        viewModelScope.launch { _searchResults.value = repo.searchAll(query) }
    }
    fun addApiConfig(config: ApiConfig) {
        viewModelScope.launch { repo.insertApiConfig(config); _apiConfigs.value = repo.getAllApiConfigs(); _defaultConfig.value = repo.getDefaultApiConfig() }
    }
    fun deleteApiConfig(config: ApiConfig) {
        viewModelScope.launch { repo.deleteApiConfig(config); _apiConfigs.value = repo.getAllApiConfigs(); _defaultConfig.value = repo.getDefaultApiConfig() }
    }
    fun setDefaultConfig(config: ApiConfig) {
        viewModelScope.launch { repo.setDefaultConfig(config); _apiConfigs.value = repo.getAllApiConfigs(); _defaultConfig.value = repo.getDefaultApiConfig() }
    }
    fun testConnection(config: ApiConfig) {
        viewModelScope.launch {
            try { val result = llm.chat(config, listOf(LlmService.ChatMsg("user", "Say OK"))); _testResult.value = "Success: ${result.take(100)}" }
            catch (e: Exception) { _testResult.value = "Error: ${e.message}" }
        }
    }

    fun addProvider(presetId: String, apiKey: String) {
        modelPool.registry.addProvider(presetId, apiKey)
        _poolStatus.value = modelPool.getStatusSummary()
        _hasProvider.value = modelPool.registry.hasAnyProvider()
    }
    fun removeProvider(providerId: String) {
        modelPool.registry.removeProvider(providerId)
        _poolStatus.value = modelPool.getStatusSummary()
        _hasProvider.value = modelPool.registry.hasAnyProvider()
    }

    fun analyzeVideo(title: String, uri: Uri) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisResult.value = null
            try {
                val analyzer = VideoAnalyzer(getApplication())
                val frames = analyzer.extractFrames(uri)
                if (frames.isEmpty()) {
                    _analysisResult.value = "\u65e0\u6cd5\u63d0\u53d6\u89c6\u9891\u5e27"
                    _isAnalyzing.value = false
                    return@launch
                }
                val prompt = analyzer.buildAnalysisPrompt(title)
                val totalModels = modelPool.registry.getTotalModelCount().first
                var lastError: Exception? = null
                for (i in 0 until totalModels) {
                    try {
                        val config = modelPool.getVisionConfig() ?: break
                        _analysisResult.value = "\u6b63\u5728\u5206\u6790\u89c6\u9891...\n\u6a21\u578b: ${config.model})"
                        val result = llm.chatWithImages(config, prompt, frames.map { it.base64 })
                        modelPool.recordVisionSuccess(config)
                        _analysisResult.value = result
                        lastError = null
                        break
                    } catch (e: Exception) {
                        lastError = e
                        val config = modelPool.getVisionConfig()
                        if (config != null) modelPool.recordVisionFailure(config)
                    }
                }
                if (lastError != null) {
                    _analysisResult.value = "\u6240\u6709\u6a21\u578b\u5747\u5931\u8d25: ${lastError?.message}"
                }
            } catch (e: Exception) {
                _analysisResult.value = "\u5206\u6790\u5931\u8d25: ${e.message}"
            } finally {
                _isAnalyzing.value = false
                _poolStatus.value = modelPool.getStatusSummary()
            }
        }
    }

    fun chatWithAI(prompt: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val totalModels = modelPool.registry.getTotalModelCount().second
            var lastError: Exception? = null
            for (i in 0 until totalModels) {
                try {
                    val config = modelPool.getTextConfig() ?: break
                    val result = llm.chat(config, listOf(LlmService.ChatMsg("user", prompt)))
                    modelPool.recordTextSuccess(config)
                    onResult(result)
                    lastError = null
                    break
                } catch (e: Exception) {
                    lastError = e
                    val config = modelPool.getTextConfig()
                    if (config != null) modelPool.recordTextFailure(config)
                }
            }
            if (lastError != null) onResult("AI \u8c03\u7528\u5931\u8d25: ${lastError?.message}")
            _poolStatus.value = modelPool.getStatusSummary()
        }
    }

    fun createMindMap(): MindMap {
        val map = MindMap()
        viewModelScope.launch { repo.saveMindMap(map); _mindMaps.value = repo.getAllMindMaps() }
        return map
    }
    fun saveMindMap(mindMap: MindMap) {
        viewModelScope.launch { repo.saveMindMap(mindMap); _mindMaps.value = repo.getAllMindMaps() }
    }
    fun deleteMindMap(mindMap: MindMap) {
        viewModelScope.launch { repo.deleteMindMap(mindMap); _mindMaps.value = repo.getAllMindMaps() }
    }
}
