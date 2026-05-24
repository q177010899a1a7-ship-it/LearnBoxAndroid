@file:OptIn(ExperimentalMaterial3Api::class)

package com.learnbox.ui.reminder

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.learnbox.data.model.Reminder
import com.learnbox.data.model.RepeatType
import com.learnbox.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(reminders: List<Reminder>, onAddReminder: (String, Long, RepeatType) -> Unit, onToggleComplete: (Reminder) -> Unit, onDeleteReminder: (Reminder) -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("提醒", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PrimaryBlue) { Icon(Icons.Default.Add, "添加提醒", tint = CardWhite) } }) { padding ->
        if (reminders.isEmpty()) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.NotificationsNone, null, Modifier.size(64.dp), tint = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Text("还没有提醒", color = TextSecondary)
            }
        }} else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reminders, key = { it.id }) { r -> ReminderCard(r, onToggleComplete, onDeleteReminder) }
            }
        }
    }
    if (showAddDialog) AddReminderDialog(onDismiss = { showAddDialog = false }, onAdd = { c, t, r -> onAddReminder(c, t, r); showAddDialog = false })
}

@Composable
fun ReminderCard(reminder: Reminder, onToggle: (Reminder) -> Unit, onDelete: (Reminder) -> Unit) {
    val sdf = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val isPast = reminder.remindAt < System.currentTimeMillis() && !reminder.isCompleted
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(reminder.isCompleted, { onToggle(reminder) }, colors = CheckboxDefaults.colors(checkedColor = SuccessGreen))
            Column(Modifier.weight(1f)) {
                Text(reminder.content, fontWeight = FontWeight.Medium, textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null, color = if (reminder.isCompleted) TextSecondary else TextPrimary)
                Text(sdf.format(Date(reminder.remindAt)), style = MaterialTheme.typography.bodySmall, color = if (isPast) WarningOrange else TextSecondary)
            }
            IconButton(onClick = { onDelete(reminder) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, "删除", tint = WarningOrange, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onAdd: (String, Long, RepeatType) -> Unit) {
    var content by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var daysFromNow by remember { mutableIntStateOf(0) }
    var repeat by remember { mutableStateOf(RepeatType.NONE) }
    val cal = remember { Calendar.getInstance() }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("添加提醒") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(content, { content = it }, label = { Text("提醒内容") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(hour.toString(), { hour = it.toIntOrNull() ?: 9 }, label = { Text("时") }, singleLine = true, modifier = Modifier.width(70.dp))
                OutlinedTextField(minute.toString(), { minute = it.toIntOrNull() ?: 0 }, label = { Text("分") }, singleLine = true, modifier = Modifier.width(70.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(daysFromNow == 0, { daysFromNow = 0 }, label = { Text("今天") })
                FilterChip(daysFromNow == 1, { daysFromNow = 1 }, label = { Text("明天") })
                FilterChip(daysFromNow == 2, { daysFromNow = 2 }, label = { Text("后天") })
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatType.values().size) { idx -> val r = RepeatType.values()[idx]; FilterChip(repeat == r, { repeat = r }, label = { Text(r.label) }) }
            }
        }},
        confirmButton = { TextButton(onClick = { if (content.isNotBlank()) { cal.timeInMillis = System.currentTimeMillis(); cal.add(Calendar.DAY_OF_YEAR, daysFromNow); cal.set(Calendar.HOUR_OF_DAY, hour); cal.set(Calendar.MINUTE, minute); cal.set(Calendar.SECOND, 0); onAdd(content, cal.timeInMillis, repeat) } }) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
