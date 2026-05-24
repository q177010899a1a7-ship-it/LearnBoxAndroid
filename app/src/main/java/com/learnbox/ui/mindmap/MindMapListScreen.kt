@file:OptIn(ExperimentalMaterial3Api::class)

package com.learnbox.ui.mindmap

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learnbox.data.model.MindMap
import com.learnbox.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapListScreen(
    mindMaps: List<MindMap>,
    onNewMindMap: () -> Unit,
    onOpenMindMap: (MindMap) -> Unit,
    onDeleteMindMap: (MindMap) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("\u601d\u7ef4\u5bfc\u56fe", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewMindMap, containerColor = Primary) {
                Icon(Icons.Default.Add, "\u65b0\u5efa", tint = Surface)
            }
        }
    ) { padding ->
        if (mindMaps.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AccountTree, null, Modifier.size(64.dp), tint = TextTertiary)
                    Spacer(Modifier.height(16.dp))
                    Text("\u8fd8\u6ca1\u6709\u601d\u7ef4\u5bfc\u56fe", color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("\u70b9\u51fb\u53f3\u4e0b\u89d2 + \u521b\u5efa\u7b2c\u4e00\u4e2a", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(mindMaps, key = { it.id }) { map ->
                    MindMapCard(map, onClick = { onOpenMindMap(map) }, onDelete = { onDeleteMindMap(map) })
                }
            }
        }
    }
}

@Composable
fun MindMapCard(map: MindMap, onClick: () -> Unit, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(48.dp), shape = MaterialTheme.shapes.medium, color = Primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AccountTree, null, tint = Primary) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(map.title.ifEmpty { "\u672a\u547d\u540d\u5bfc\u56fe" }, fontWeight = FontWeight.Medium)
                Text("${map.nodes.size} \u4e2a\u8282\u70b9", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "\u5220\u9664", tint = Error) }
        }
    }
}
