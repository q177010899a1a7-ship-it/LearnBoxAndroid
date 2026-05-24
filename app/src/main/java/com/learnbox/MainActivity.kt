package com.learnbox

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.learnbox.data.model.VideoPlatform
import com.learnbox.ui.MainViewModel
import com.learnbox.ui.navigation.MainScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    val videos by viewModel.videos.collectAsState()
                    val notes by viewModel.notes.collectAsState()
                    val reminders by viewModel.reminders.collectAsState()
                    val searchResults by viewModel.searchResults.collectAsState()
                    val apiConfigs by viewModel.apiConfigs.collectAsState()
                    val defaultConfig by viewModel.defaultConfig.collectAsState()
                    val testResult by viewModel.testResult.collectAsState()
                    val mindMaps by viewModel.mindMaps.collectAsState()
                    val analysisResult by viewModel.analysisResult.collectAsState()
                    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
                    val poolStatus by viewModel.poolStatus.collectAsState()
                    val hasProvider by viewModel.hasProvider.collectAsState()
                    val activeProviders = viewModel.modelPool.registry.getActiveProviders()
                    val availablePresets = viewModel.modelPool.registry.getAvailablePresets()

                    val videoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (_: Exception) {}
                            var title = "\u672c\u5730\u89c6\u9891"
                            try {
                                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    if (cursor.moveToFirst() && nameIndex >= 0) {
                                        title = cursor.getString(nameIndex) ?: title
                                    }
                                }
                            } catch (_: Exception) {}
                            viewModel.addVideo(uri.toString(), VideoPlatform.OTHER, title)
                        }
                    }

                    MainScreen(
                        videos = videos,
                        notes = notes,
                        reminders = reminders,
                        searchResults = searchResults,
                        apiConfigs = apiConfigs,
                        defaultConfig = defaultConfig,
                        mindMaps = mindMaps,
                        poolStatus = poolStatus,
                        activeProviders = activeProviders,
                        availablePresets = availablePresets,
                        hasProvider = hasProvider,
                        onAddVideo = viewModel::addVideo,
                        onVideoStatusChange = viewModel::changeVideoStatus,
                        onDeleteVideo = viewModel::deleteVideo,
                        onAddNote = viewModel::addNote,
                        onUpdateNote = viewModel::updateNote,
                        onDeleteNote = viewModel::deleteNote,
                        onAddReminder = viewModel::addReminder,
                        onToggleReminder = viewModel::toggleReminder,
                        onDeleteReminder = viewModel::deleteReminder,
                        onSearch = viewModel::search,
                        onAddConfig = viewModel::addApiConfig,
                        onDeleteConfig = viewModel::deleteApiConfig,
                        onSetDefault = viewModel::setDefaultConfig,
                        onTestConnection = viewModel::testConnection,
                        testResult = testResult,
                        onAddProvider = viewModel::addProvider,
                        onRemoveProvider = viewModel::removeProvider,
                        onPickLocalVideo = { videoPickerLauncher.launch(arrayOf("video/*")) },
                        onCreateMindMap = { viewModel.createMindMap() },
                        onOpenMindMap = {},
                        onSaveMindMap = viewModel::saveMindMap,
                        onDeleteMindMap = viewModel::deleteMindMap,
                        onAnalyzeVideo = viewModel::analyzeVideo,
                        analysisResult = analysisResult,
                        isAnalyzing = isAnalyzing
                    )
                }
            }
        }
    }
}
