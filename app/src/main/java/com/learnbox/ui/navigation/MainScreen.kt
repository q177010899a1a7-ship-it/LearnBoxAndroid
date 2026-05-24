package com.learnbox.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.learnbox.data.model.*
import com.learnbox.service.AIProvider
import com.learnbox.ui.mindmap.MindMapEditorScreen
import com.learnbox.ui.mindmap.MindMapListScreen
import com.learnbox.ui.note.NoteListScreen
import com.learnbox.ui.reminder.ReminderListScreen
import com.learnbox.ui.search.SearchScreen
import com.learnbox.ui.settings.SettingsScreen
import com.learnbox.ui.setup.SetupScreen
import com.learnbox.ui.video.VideoAnalysisScreen
import com.learnbox.ui.video.VideoListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    videos: List<Video>, notes: List<Note>, reminders: List<Reminder>,
    searchResults: List<SearchResult>, apiConfigs: List<ApiConfig>, defaultConfig: ApiConfig?,
    mindMaps: List<MindMap>,
    poolStatus: String,
    activeProviders: List<AIProvider>,
    availablePresets: List<AIProvider>,
    hasProvider: Boolean,
    onAddVideo: (String, VideoPlatform, String) -> Unit,
    onVideoStatusChange: (Video, WatchStatus) -> Unit, onDeleteVideo: (Video) -> Unit,
    onAddNote: (String, String, NoteTemplateType?) -> Unit, onUpdateNote: (Note) -> Unit, onDeleteNote: (Note) -> Unit,
    onAddReminder: (String, Long, RepeatType) -> Unit, onToggleReminder: (Reminder) -> Unit, onDeleteReminder: (Reminder) -> Unit,
    onSearch: (String) -> Unit,
    onAddConfig: (ApiConfig) -> Unit, onDeleteConfig: (ApiConfig) -> Unit, onSetDefault: (ApiConfig) -> Unit,
    onTestConnection: (ApiConfig) -> Unit, testResult: String?,
    onAddProvider: (String, String) -> Unit, onRemoveProvider: (String) -> Unit,
    onPickLocalVideo: () -> Unit,
    onCreateMindMap: () -> Unit, onOpenMindMap: (MindMap) -> Unit, onSaveMindMap: (MindMap) -> Unit, onDeleteMindMap: (MindMap) -> Unit,
    onAnalyzeVideo: (String, android.net.Uri) -> Unit, analysisResult: String?, isAnalyzing: Boolean
) {
    // Show setup screen if no provider configured
    if (!hasProvider) {
        SetupScreen(
            onAddProvider = onAddProvider,
            onSkipToMain = { /* will auto-switch when hasProvider becomes true */ }
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var analyzingVideo by remember { mutableStateOf<Pair<String, android.net.Uri>?>(null) }
    var editingMindMap by remember { mutableStateOf<MindMap?>(null) }

    if (analyzingVideo != null) {
        val (title, uri) = analyzingVideo!!
        VideoAnalysisScreen(
            videoTitle = title,
            analysisResult = analysisResult,
            isAnalyzing = isAnalyzing,
            onAnalyze = { onAnalyzeVideo(title, uri) },
            onBack = { analyzingVideo = null },
            onSaveToNote = { content -> onAddNote("视频分析: $title", content, null) }
        )
        return
    }
    if (editingMindMap != null) {
        MindMapEditorScreen(
            mindMap = editingMindMap!!,
            onUpdate = { onSaveMindMap(it) },
            onBack = { editingMindMap = null }
        )
        return
    }

    val tabs = listOf(
        Triple("videos", "\u89c6\u9891", Icons.Default.PlayArrow),
        Triple("notes", "\u7b14\u8bb0", Icons.Default.Description),
        Triple("mindmaps", "\u5bfc\u56fe", Icons.Default.AccountTree),
        Triple("reminders", "\u63d0\u9192", Icons.Default.Notifications),
        Triple("search", "\u641c\u7d22", Icons.Default.Search),
        Triple("settings", "\u8bbe\u7f6e", Icons.Default.Settings)
    )
    Scaffold(bottomBar = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            tabs.forEach { (route, label, icon) ->
                NavigationBarItem(
                    icon = { Icon(icon, label) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    selected = currentRoute == route,
                    onClick = { navController.navigate(route) { popUpTo("videos") { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = com.learnbox.ui.theme.Primary,
                        selectedTextColor = com.learnbox.ui.theme.Primary,
                        indicatorColor = com.learnbox.ui.theme.PrimaryContainer
                    )
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "videos", modifier = Modifier.padding(padding)) {
            composable("videos") { VideoListScreen(videos, onAddVideo, onVideoStatusChange, onDeleteVideo, onAnalyzeVideo = { title, uri -> analyzingVideo = Pair(title, uri) }, onPickLocalVideo = onPickLocalVideo) }
            composable("notes") { NoteListScreen(notes, onAddNote, onUpdateNote, onDeleteNote) }
            composable("mindmaps") { MindMapListScreen(mindMaps, onNewMindMap = {
                editingMindMap = com.learnbox.data.model.MindMap()
            }, onOpenMindMap = { editingMindMap = it }, onDeleteMindMap = { onDeleteMindMap(it) }) }
            composable("reminders") { ReminderListScreen(reminders, onAddReminder, onToggleReminder, onDeleteReminder) }
            composable("search") { SearchScreen(searchResults, onSearch) }
            composable("settings") { SettingsScreen(
                apiConfigs, defaultConfig, poolStatus, activeProviders, availablePresets,
                onAddConfig, onDeleteConfig, onSetDefault, onTestConnection, testResult,
                onAddProvider, onRemoveProvider
            ) }
        }
    }
}
