@file:OptIn(ExperimentalMaterial3Api::class)

package com.learnbox.ui.settings

import androidx.compose.foundation.clickable
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
import com.learnbox.data.model.ApiConfig
import com.learnbox.service.AIProvider
import com.learnbox.service.ProviderRegistry
import com.learnbox.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    apiConfigs: List<ApiConfig>,
    defaultConfig: ApiConfig?,
    poolStatus: String,
    activeProviders: List<AIProvider>,
    availablePresets: List<AIProvider>,
    onAddConfig: (ApiConfig) -> Unit,
    onDeleteConfig: (ApiConfig) -> Unit,
    onSetDefault: (ApiConfig) -> Unit,
    onTestConnection: (ApiConfig) -> Unit,
    testResult: String?,
    onAddProvider: (String, String) -> Unit,
    onRemoveProvider: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showProviderDialog by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("\u8bbe\u7f6e", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Model Pool Status
            item {
                Text("\u6a21\u578b\u6c60\u72b6\u6001", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PrimaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("\u667a\u80fd\u6a21\u578b\u8f6e\u8be2", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(poolStatus, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("\u81ea\u52a8\u5728\u591a\u4e2a\u514d\u8d39\u6a21\u578b\u4e4b\u95f4\u8f6e\u8be2\uff0c\u5931\u8d25\u81ea\u52a8\u5207\u6362", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }

            // Active Providers
            item {
                Text("\u5df2\u542f\u7528\u4f9b\u5e94\u5546", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(activeProviders, key = { it.id }) { provider ->
                ProviderCard(provider, onRemove = { onRemoveProvider(provider.id) })
            }
            item {
                if (availablePresets.isNotEmpty()) {
                    Card(onClick = { showProviderDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("\u6dfb\u52a0\u514d\u8d39\u4f9b\u5e94\u5546", fontWeight = FontWeight.Medium)
                                Text("Groq / Gemini / SambaNova", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Custom API Configs
            item { Text("\u81ea\u5b9a\u4e49 API", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            item {
                Card(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, tint = Primary)
                        Spacer(Modifier.width(12.dp))
                        Text("\u6dfb\u52a0 API \u914d\u7f6e")
                    }
                }
            }
            items(apiConfigs, key = { it.id }) { config ->
                ApiConfigCard(config, config.id == defaultConfig?.id, onDeleteConfig, onSetDefault, onTestConnection)
            }

            // Test result
            if (testResult != null) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(testResult, Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // About
            item { Text("\u5173\u4e8e", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("LearnBox", fontWeight = FontWeight.Bold)
                                Text("\u89c6\u9891\u5b66\u4e60\u7b14\u8bb0\u672c v2.5", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) AddApiConfigDialog(onDismiss = { showAddDialog = false }, onAdd = { onAddConfig(it); showAddDialog = false })
    if (showProviderDialog) AddProviderDialog(
        presets = availablePresets,
        onDismiss = { showProviderDialog = false },
        onAdd = { id, key -> onAddProvider(id, key); showProviderDialog = false }
    )
}

@Composable
fun ProviderCard(provider: AIProvider, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(provider.name, fontWeight = FontWeight.Medium)
                    Text(provider.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Surface(shape = MaterialTheme.shapes.small, color = Success.copy(alpha = 0.15f)) {
                    Text("\u5df2\u542f\u7528", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Success, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (provider.visionModels.isNotEmpty()) {
                    Text("\u89c6\u89c9: ${'$'}{provider.visionModels.size}\u4e2a", style = MaterialTheme.typography.bodySmall, color = Info)
                }
                Text("\u6587\u672c: ${'$'}{provider.textModels.size}\u4e2a", style = MaterialTheme.typography.bodySmall, color = Info)
            }
            if (provider.id != "siliconflow") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRemove, modifier = Modifier.height(32.dp)) {
                    Text("\u79fb\u9664", style = MaterialTheme.typography.labelSmall, color = Error)
                }
            }
        }
    }
}

@Composable
fun AddProviderDialog(presets: List<AIProvider>, onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var selectedPreset by remember { mutableStateOf<AIProvider?>(null) }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\u6dfb\u52a0\u514d\u8d39\u4f9b\u5e94\u5546") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (selectedPreset == null) {
                    Text("\u9009\u62e9\u4e00\u4e2a\u4f9b\u5e94\u5546:", style = MaterialTheme.typography.bodyMedium)
                    presets.forEach { preset ->
                        Card(
                            onClick = { selectedPreset = preset },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(preset.name, fontWeight = FontWeight.Medium)
                                Text(preset.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (preset.visionModels.isNotEmpty()) {
                                        Text("\u89c6\u89c9 ${'$'}{preset.visionModels.size}\u4e2a", style = MaterialTheme.typography.labelSmall, color = Info)
                                    }
                                    Text("\u6587\u672c ${'$'}{preset.textModels.size}\u4e2a", style = MaterialTheme.typography.labelSmall, color = Info)
                                }
                            }
                        }
                    }
                } else {
                    Text("${'$'}{selectedPreset!!.name}", fontWeight = FontWeight.Bold)
                    Text(selectedPreset!!.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("\u8bf7\u8f93\u5165 API Key:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("\u83b7\u53d6\u65b9\u5f0f: \u8bbf\u95ee ${'$'}{selectedPreset!!.name} \u5b98\u7f51\u6ce8\u518c\u514d\u8d39\u8d26\u53f7", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            }
        },
        confirmButton = {
            if (selectedPreset != null) {
                TextButton(onClick = { if (apiKey.isNotBlank()) onAdd(selectedPreset!!.id, apiKey) }) { Text("\u6dfb\u52a0") }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (selectedPreset == null) onDismiss() else { selectedPreset = null; apiKey = "" } }) {
                Text(if (selectedPreset == null) "\u53d6\u6d88" else "\u8fd4\u56de")
            }
        }
    )
}

@Composable
fun ApiConfigCard(config: ApiConfig, isDefault: Boolean, onDelete: (ApiConfig) -> Unit, onSetDefault: (ApiConfig) -> Unit, onTest: (ApiConfig) -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartToy, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text(config.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                if (isDefault) { Surface(shape = MaterialTheme.shapes.small, color = Success.copy(alpha = 0.15f)) { Text("\u9ed8\u8ba4", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Success, style = MaterialTheme.typography.labelSmall) } }
            }
            Text("\u6a21\u578b: " + config.model, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("\u5730\u5740: " + config.baseUrl, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onTest(config) }, modifier = Modifier.height(32.dp)) { Text("\u6d4b\u8bd5", style = MaterialTheme.typography.labelSmall) }
                if (!isDefault) { OutlinedButton(onClick = { onSetDefault(config) }, modifier = Modifier.height(32.dp)) { Text("\u8bbe\u4e3a\u9ed8\u8ba4", style = MaterialTheme.typography.labelSmall) } }
                OutlinedButton(onClick = { onDelete(config) }, modifier = Modifier.height(32.dp)) { Text("\u5220\u9664", style = MaterialTheme.typography.labelSmall, color = Error) }
            }
        }
    }
}

@Composable
fun AddApiConfigDialog(onDismiss: () -> Unit, onAdd: (ApiConfig) -> Unit) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var maxTokens by remember { mutableStateOf("1000") }
    var temperature by remember { mutableStateOf("0.7") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("\u6dfb\u52a0 API \u914d\u7f6e") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("\u914d\u7f6e\u540d\u79f0") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(baseUrl, { baseUrl = it }, label = { Text("API \u5730\u5740") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(apiKey, { apiKey = it }, label = { Text("API \u5bc6\u94a5") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(model, { model = it }, label = { Text("\u6a21\u578b\u540d\u79f0") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(maxTokens, { maxTokens = it }, label = { Text("MaxTokens") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(temperature, { temperature = it }, label = { Text("\u6e29\u5ea6") }, singleLine = true, modifier = Modifier.weight(1f))
            }
        }},
        confirmButton = { TextButton(onClick = { if (name.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank()) onAdd(ApiConfig(name = name, baseUrl = baseUrl, apiKey = apiKey, model = model, maxTokens = maxTokens.toIntOrNull() ?: 1000, temperature = temperature.toDoubleOrNull() ?: 0.7)) }) { Text("\u6dfb\u52a0") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") } }
    )
}

