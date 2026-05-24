package com.learnbox.ui.video

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learnbox.data.model.Video
import com.learnbox.data.model.VideoPlatform
import com.learnbox.data.model.WatchStatus
import com.learnbox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    videos: List<Video>,
    onAddVideo: (String, VideoPlatform, String) -> Unit,
    onStatusChange: (Video, WatchStatus) -> Unit,
    onDeleteVideo: (Video) -> Unit,
    onAnalyzeVideo: ((String, Uri) -> Unit)? = null,
    onPickLocalVideo: (() -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<WatchStatus?>(null) }
    val filtered = if (selectedFilter == null) videos else videos.filter { it.status == selectedFilter }

    Scaffold(
        topBar = { TopAppBar(title = { Text("\u89c6\u9891", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Pick local video button
                if (onPickLocalVideo != null) {
                    SmallFloatingActionButton(onClick = onPickLocalVideo, containerColor = Info) {
                        Icon(Icons.Default.FolderOpen, "\u9009\u62e9\u672c\u5730\u89c6\u9891", tint = Surface)
                    }
                }
                // Add web URL button
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Primary) {
                    Icon(Icons.Default.Add, "\u6dfb\u52a0\u89c6\u9891", tint = Surface)
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                item { FilterChip(selectedFilter == null, { selectedFilter = null }, label = { Text("\u5168\u90e8") }) }
                items(WatchStatus.values()) { status ->
                    FilterChip(selectedFilter == status, { selectedFilter = if (selectedFilter == status) null else status }, label = { Text(status.label) })
                }
            }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayCircle, null, Modifier.size(64.dp), tint = TextTertiary)
                        Spacer(Modifier.height(16.dp))
                        Text("\u8fd8\u6ca1\u6709\u672c\u5730\u89c6\u9891", color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        if (onPickLocalVideo != null) {
                            Button(onClick = onPickLocalVideo, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                                Icon(Icons.Default.FolderOpen, null)
                                Spacer(Modifier.width(8.dp))
                                Text("\u9009\u62e9\u672c\u5730\u89c6\u9891")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("\u70b9\u51fb\u53f3\u4e0b\u89d2\u6309\u94ae\u6dfb\u52a0", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered, key = { it.id }) { video ->
                        VideoCard(video, onStatusChange, onDeleteVideo, onAnalyzeVideo)
                    }
                }
            }
        }
    }
    if (showAddDialog) AddVideoDialog(onDismiss = { showAddDialog = false }, onAdd = { url, platform, title ->
        onAddVideo(url, platform, title)
        showAddDialog = false
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoCard(video: Video, onStatusChange: (Video, WatchStatus) -> Unit, onDelete: (Video) -> Unit, onAnalyze: ((String, Uri) -> Unit)? = null) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor = when (video.status) {
        WatchStatus.UNWATCHED -> StatusUnwatched
        WatchStatus.WATCHING -> StatusWatching
        WatchStatus.WATCHED -> StatusWatched
    }
    val isLocalVideo = video.url.startsWith("content://") || video.url.startsWith("file://") || video.url.startsWith("/")
    Card(Modifier.fillMaxWidth().clickable { expanded = !expanded }, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.small, color = Primary.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isLocalVideo) Icons.Default.PhoneAndroid else Icons.Default.PlayArrow,
                            null, tint = Primary
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(video.title, fontWeight = FontWeight.Medium, maxLines = 2)
                    Text(
                        if (isLocalVideo) "\u672c\u5730\u89c6\u9891" else video.platform.label,
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary
                    )
                }
                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.15f)) {
                    Text(video.status.label, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = statusColor, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (expanded) {
                Divider(color = Divider)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    WatchStatus.values().forEach { status ->
                        TextButton(onClick = { onStatusChange(video, status) }) { Text(status.label) }
                    }
                    IconButton(onClick = { onDelete(video) }) { Icon(Icons.Default.Delete, "\u5220\u9664", tint = Warning) }
                    if (onAnalyze != null) {
                        IconButton(onClick = {
                            val uri = Uri.parse(video.url)
                            onAnalyze(video.title, uri)
                        }) {
                            Icon(Icons.Default.AutoAwesome, "\u5206\u6790", tint = Primary)
                        }
                    } else if (onAnalyze != null && !isLocalVideo) {
                        Text("\u4ec5\u652f\u6301\u672c\u5730\u89c6\u9891", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoDialog(onDismiss: () -> Unit, onAdd: (String, VideoPlatform, String) -> Unit) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf(VideoPlatform.DOUYIN) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("\u6dfb\u52a0\u89c6\u9891") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(url, { url = it }, label = { Text("\u89c6\u9891\u94fe\u63a5") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(title, { title = it }, label = { Text("\u6807\u9898") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoPlatform.values()) { p -> FilterChip(platform == p, { platform = p }, label = { Text(p.label) }) }
            }
        }},
        confirmButton = { TextButton(onClick = { if (url.isNotBlank() && title.isNotBlank()) onAdd(url, platform, title) }) { Text("\u6dfb\u52a0") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") } }
    )
}
