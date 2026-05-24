@file:OptIn(ExperimentalMaterial3Api::class)

package com.learnbox.ui.note

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
import com.learnbox.data.model.Note
import com.learnbox.data.model.NoteTemplateType
import com.learnbox.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(notes: List<Note>, onAddNote: (String, String, NoteTemplateType?) -> Unit, onUpdateNote: (Note) -> Unit, onDeleteNote: (Note) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("笔记", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PrimaryBlue) { Icon(Icons.Default.Add, "新建笔记", tint = CardWhite) } }) { padding ->
        if (notes.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Description, null, Modifier.size(64.dp), tint = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Text("还没有笔记", color = TextSecondary)
            }
        }} else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(notes, key = { it.id }) { note -> NoteCard(note, onClick = { editingNote = note }, onDelete = { onDeleteNote(note) }) }
            }
        }
    }
    if (showAddDialog) AddNoteDialog(onDismiss = { showAddDialog = false }, onAdd = { t, c, tmpl -> onAddNote(t, c, tmpl); showAddDialog = false })
    editingNote?.let { note -> EditNoteDialog(note, onDismiss = { editingNote = null }, onSave = { onUpdateNote(it); editingNote = null }) }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(note.title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "删除", tint = WarningOrange, modifier = Modifier.size(18.dp)) }
            }
            if (note.content.isNotBlank()) { Text(note.content.take(100), style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2) }
            Row { note.template?.let { Text(it.label, style = MaterialTheme.typography.labelSmall, color = PrimaryBlue) }; Text(sdf.format(Date(note.updatedAt)), style = MaterialTheme.typography.labelSmall, color = TextSecondary) }
        }
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String, NoteTemplateType?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("新建笔记") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(content, { content = it }, label = { Text("内容") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6)
        }},
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onAdd(title, content, null) }) { Text("创建") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun EditNoteDialog(note: Note, onDismiss: () -> Unit, onSave: (Note) -> Unit) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("编辑笔记") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(content, { content = it }, label = { Text("内容") }, modifier = Modifier.fillMaxWidth().height(200.dp), maxLines = 10)
        }},
        confirmButton = { TextButton(onClick = { onSave(note.copy(title = title, content = content, updatedAt = System.currentTimeMillis())) }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
