package com.learnbox.ui.mindmap

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learnbox.data.model.MindMap
import com.learnbox.data.model.MindNode
import com.learnbox.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapEditorScreen(
    mindMap: MindMap,
    onUpdate: (MindMap) -> Unit,
    onBack: () -> Unit
) {
    var currentMap by remember(mindMap) { mutableStateOf(mindMap) }
    var editingNode by remember { mutableStateOf<MindNode?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedParent by remember { mutableStateOf<MindNode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentMap.title.ifEmpty { "\u65b0\u5efa\u5bfc\u56fe" }, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onUpdate(currentMap); onBack() }) {
                        Icon(Icons.Default.ArrowBack, "\u8fd4\u56de")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val root = currentMap.nodes.firstOrNull { it.parentId == null }
                        if (root == null) {
                            val newRoot = MindNode(text = "\u4e2b\u9898", colorIndex = 0)
                            currentMap = currentMap.copy(nodes = listOf(newRoot), updatedAt = System.currentTimeMillis())
                        }
                    }) { Icon(Icons.Default.CenterFocusStrong, "\u5c45\u4e2d") }
                }
            )
        },
        floatingActionButton = {
            Column {
                SmallFloatingActionButton(
                    onClick = {
                        selectedParent = null
                        showAddDialog = true
                    },
                    containerColor = Primary
                ) { Icon(Icons.Default.Add, "\u6dfb\u52a0\u6839\u8282\u70b9", tint = Surface) }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (currentMap.nodes.isEmpty()) {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text("\u70b9\u51fb\u53f3\u4e0b\u89d2 + \u521b\u5efa\u6839\u8282\u70b9", color = TextSecondary)
                    }
                }
            } else {
                MindMapCanvas(
                    mindMap = currentMap,
                    onNodeClick = { node ->
                        selectedParent = node
                        showAddDialog = true
                    },
                    onNodeDrag = { node, x, y ->
                        val updated = currentMap.nodes.map {
                            if (it.id == node.id) it.copy(x = x, y = y) else it
                        }
                        currentMap = currentMap.copy(nodes = updated, updatedAt = System.currentTimeMillis())
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (selectedParent != null) "\u6dfb\u52a0\u5b50\u8282\u70b9" else "\u6dfb\u52a0\u6839\u8282\u70b9") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("\u8282\u70b9\u5185\u5bb9") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        val depth = if (selectedParent != null) {
                            var d = 0
                            var p = selectedParent
                            while (p != null) { d++; p = currentMap.nodes.firstOrNull { it.id == p?.parentId } }
                            d
                        } else 0
                        val newNode = MindNode(
                            text = text,
                            parentId = selectedParent?.id,
                            colorIndex = (depth).coerceIn(0, MindNodeColors.size - 1)
                        )
                        currentMap = currentMap.copy(
                            nodes = currentMap.nodes + newNode,
                            updatedAt = System.currentTimeMillis()
                        )
                        if (currentMap.title.isEmpty() && selectedParent == null) {
                            currentMap = currentMap.copy(title = text)
                        }
                        showAddDialog = false
                    }
                }) { Text("\u6dfb\u52a0") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("\u53d6\u6d88") }
            }
        )
    }
}
