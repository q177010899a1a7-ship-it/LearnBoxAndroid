@file:OptIn(ExperimentalMaterial3Api::class)

package com.learnbox.ui.search

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
import com.learnbox.data.model.SearchResult
import com.learnbox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(results: List<SearchResult>, onSearch: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("搜索", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it },
                placeholder = { Text("搜索笔记、视频、提醒...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onSearch(query) }, modifier = Modifier.fillMaxWidth(), enabled = query.isNotBlank()) { Text("搜索") }
            Spacer(Modifier.height(16.dp))
            if (results.isEmpty() && query.isNotBlank()) {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) { Text("没有找到结果", color = TextSecondary) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { result -> SearchResultCard(result) }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(result: SearchResult) {
    val icon = when (result.type) { "video" -> Icons.Default.PlayCircle; "note" -> Icons.Default.Description; else -> Icons.Default.Notifications }
    val color = when (result.type) { "video" -> PrimaryBlue; "note" -> SuccessGreen; else -> WarningOrange }
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(result.title, fontWeight = FontWeight.Medium)
                Text(result.snippet, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
