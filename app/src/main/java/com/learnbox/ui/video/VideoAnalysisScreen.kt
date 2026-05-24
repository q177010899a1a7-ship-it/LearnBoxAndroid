package com.learnbox.ui.video

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnbox.ui.theme.*

data class AnalysisSection(
    val title: String,
    val icon: String,
    val content: String,
    val isExpanded: Boolean = true
)

fun parseAnalysisResult(raw: String): List<AnalysisSection> {
    val sections = mutableListOf<AnalysisSection>()
    val patterns = listOf(
        Triple("\u3010\u89c6\u9891\u6458\u8981\u3011", "\ud83d\udcdd", "\u89c6\u9891\u6458\u8981"),
        Triple("\u3010\u5173\u952e\u8981\u70b9\u3011", "\u2b50", "\u5173\u952e\u8981\u70b9"),
        Triple("\u3010\u8be6\u7ec6\u7b14\u8bb0\u3011", "\ud83d\udcd3", "\u8be6\u7ec6\u7b14\u8bb0"),
        Triple("\u3010\u601d\u7ef4\u5bfc\u56fe\u3011", "\ud83c\udf10", "\u601d\u7ef4\u5bfc\u56fe"),
        Triple("\u3010\u5b66\u4e60\u5efa\u8bae\u3011", "\ud83d\udca1", "\u5b66\u4e60\u5efa\u8bae")
    )
    for ((marker, icon, title) in patterns) {
        val idx = raw.indexOf(marker)
        if (idx >= 0) {
            val start = idx + marker.length
            val nextIdx = patterns.map { raw.indexOf(it.first, start) }.filter { it > start }.minOrNull() ?: raw.length
            val sectionContent = raw.substring(start, nextIdx).trim()
            if (sectionContent.isNotBlank()) {
                sections.add(AnalysisSection(title, icon, sectionContent))
            }
        }
    }
    if (sections.isEmpty() && raw.isNotBlank()) {
        sections.add(AnalysisSection("\u5206\u6790\u7ed3\u679c", "\ud83d\udcdd", raw))
    }
    return sections
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAnalysisScreen(
    videoTitle: String,
    analysisResult: String?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onBack: () -> Unit,
    onSaveToNote: ((String) -> Unit)? = null
) {
    val sections = remember(analysisResult) { analysisResult?.let { parseAnalysisResult(it) } ?: emptyList() }
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    var checkedItems by remember { mutableStateOf(setOf<String>()) }
    var highlightedItems by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(sections) {
        expandedSections = sections.map { it.title }.toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\u89c6\u9891\u5206\u6790", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "\u8fd4\u56de") }
                },
                actions = {
                    if (analysisResult != null && onSaveToNote != null) {
                        IconButton(onClick = {
                            val sb = StringBuilder()
                            for (section in sections) {
                                sb.appendLine("## ${section.title}")
                                val lines = section.content.split("\n")
                                for (line in lines) {
                                    val trimmed = line.trim()
                                    if (trimmed.isNotBlank()) {
                                        val key = "${section.title}:$trimmed"
                                        val mark = if (key in checkedItems) "[!] " else if (key in highlightedItems) "[*] " else ""
                                        sb.appendLine("$mark$trimmed")
                                    }
                                }
                                sb.appendLine()
                            }
                            onSaveToNote(sb.toString())
                        }) {
                            Icon(Icons.Default.SaveAlt, "\u4fdd\u5b58\u7b14\u8bb0")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Video info
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PrimaryContainer)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayCircle, null, tint = Primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(videoTitle, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }

            if (isAnalyzing) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\u23f3", fontSize = 48.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("\u6b63\u5728\u5206\u6790\u89c6\u9891...", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("\u63d0\u53d6\u5173\u952e\u5e27 + AI \u5206\u6790\uff0c\u8bf7\u7a0d\u5019", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else if (sections.isNotEmpty()) {
                // Check count badge
                if (checkedItems.isNotEmpty() || highlightedItems.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("\u5df2\u6807\u8bb0 ${checkedItems.size} \u9879\u91cd\u70b9 + ${highlightedItems.size} \u9879\u9ad8\u4eae", fontSize = 13.sp, color = Primary)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { checkedItems = emptySet(); highlightedItems = emptySet() }) {
                                Text("\u6e05\u9664", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Sections
                for (section in sections) {
                    val isExpanded = section.title in expandedSections
                    AnalysisSectionCard(
                        section = section,
                        isExpanded = isExpanded,
                        checkedItems = checkedItems,
                        highlightedItems = highlightedItems,
                        onToggleExpand = {
                            expandedSections = if (isExpanded) expandedSections - section.title
                            else expandedSections + section.title
                        },
                        onToggleCheck = { line ->
                            val key = "${section.title}:$line"
                            checkedItems = if (key in checkedItems) checkedItems - key else checkedItems + key
                        },
                        onToggleHighlight = { line ->
                            val key = "${section.title}:$line"
                            highlightedItems = if (key in highlightedItems) highlightedItems - key else highlightedItems + key
                        }
                    )
                }
            } else {
                // Empty state - show analyze button
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(56.dp), tint = Primary)
                        Spacer(Modifier.height(16.dp))
                        Text("AI \u89c6\u9891\u5206\u6790", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("\u81ea\u52a8\u63d0\u53d6\u89c6\u9891\u5173\u952e\u5e27\uff0c\u751f\u6210\u4e2d\u6587\u6458\u8981\u3001\u7b14\u8bb0\u548c\u601d\u7ef4\u5bfc\u56fe", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onAnalyze,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null)
                            Spacer(Modifier.width(8.dp))
                            Text("\u5f00\u59cb\u5206\u6790", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnalysisSectionCard(
    section: AnalysisSection,
    isExpanded: Boolean,
    checkedItems: Set<String>,
    highlightedItems: Set<String>,
    onToggleExpand: () -> Unit,
    onToggleCheck: (String) -> Unit,
    onToggleHighlight: (String) -> Unit
) {
    val sectionColor = when (section.title) {
        "\u89c6\u9891\u6458\u8981" -> Info
        "\u5173\u952e\u8981\u70b9" -> Warning
        "\u8be6\u7ec6\u7b14\u8bb0" -> Primary
        "\u601d\u7ef4\u5bfc\u56fe" -> Success
        "\u5b66\u4e60\u5efa\u8bae" -> Color(0xFF8B5CF6)
        else -> Primary
    }

    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth().clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(section.icon, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(section.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(6.dp), color = sectionColor.copy(alpha = 0.1f)) {
                    Text(
                        if (isExpanded) "\u6536\u8d77" else "\u5c55\u5f00",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = sectionColor
                    )
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Border.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))

                val lines = section.content.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                for (line in lines) {
                    val key = "${section.title}:$line"
                    val isChecked = key in checkedItems
                    val isHighlighted = key in highlightedItems

                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Checkbox
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleCheck(line) },
                            modifier = Modifier.size(20.dp),
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Spacer(Modifier.width(8.dp))

                        // Text with highlight
                        val bgColor = if (isHighlighted) Warning.copy(alpha = 0.15f) else Color.Transparent
                        val textColor = if (isChecked) Primary else TextPrimary
                        val weight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                        val decoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None

                        Text(
                            text = line,
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = weight,
                            textDecoration = decoration,
                            lineHeight = 20.sp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clickable { onToggleHighlight(line) }
                        )
                    }
                }
            }
        }
    }
}





