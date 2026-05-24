package com.learnbox.ui.setup

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnbox.service.AIProvider
import com.learnbox.service.ProviderRegistry
import com.learnbox.ui.theme.*

private const val SF_REFERRAL = "https://cloud.siliconflow.cn/i/cghBZBng"

@Composable
fun SetupScreen(
    onAddProvider: (String, String) -> Unit,
    onSkipToMain: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf<AIProvider?>(null) }
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF818CF8),
                        Color(0xFFEEF2FF)
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoStories, null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("LearnBox", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("AI \u9a71\u52a8\u7684\u667a\u80fd\u5b66\u4e60\u7b14\u8bb0", fontSize = 15.sp, color = Color.White.copy(alpha = 0.85f))

            Spacer(Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedPreset == null) {
                        Text("\u9009\u62e9 AI \u670d\u52a1\u5546", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("\u914d\u7f6e\u4e00\u4e2a\u63d0\u4f9b\u5546\u5373\u53ef\u5f00\u59cb\u4f7f\u7528", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

                        Spacer(Modifier.height(20.dp))

                        // SiliconFlow recommended
                        val sfPreset = ProviderRegistry.PRESETS.first { it.id == "siliconflow" }
                        Surface(
                            onClick = { selectedPreset = sfPreset },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF6366F1).copy(alpha = 0.08f),
                            border = BorderStroke(2.dp, Color(0xFF6366F1).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF6366F1).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Cloud, null, tint = Color(0xFF6366F1), modifier = Modifier.size(28.dp)) }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("SiliconFlow", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Spacer(Modifier.width(8.dp))
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF6366F1)) {
                                                Text("\u63a8\u8350", Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text("\u56fd\u5185\u9996\u9009 \u00b7 \u514d\u8d39\u6a21\u578b\u6700\u591a", fontSize = 13.sp, color = TextSecondary)
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(24.dp))
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Info.copy(alpha = 0.1f)) {
                                        Text("6 \u89c6\u89c9\u6a21\u578b", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 12.sp, color = Info)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = Success.copy(alpha = 0.1f)) {
                                        Text("10+ \u6587\u672c", Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 12.sp, color = Success)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Surface(shape = RoundedCornerShape(8.dp), color = Warning.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                                    Text("\u5145\u503c 2 \u5143\u5373\u53ef\u4f53\u9a8c\u5168\u90e8\u529f\u80fd", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 12.sp, color = Warning, fontWeight = FontWeight.Medium)
                                }

                                Spacer(Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SF_REFERRAL))) },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp), tint = Color(0xFF6366F1))
                                    Spacer(Modifier.width(6.dp))
                                    Text("\u70b9\u51fb\u6ce8\u518c\u514d\u8d39\u8d26\u53f7 (\u9080\u8bf7\u94fe\u63a5)", fontSize = 13.sp, color = Color(0xFF6366F1))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Divider(modifier = Modifier.weight(1f), color = Border)
                            Text("  \u6216\u9009\u62e9\u5176\u4ed6  ", fontSize = 12.sp, color = TextTertiary)
                            Divider(modifier = Modifier.weight(1f), color = Border)
                        }

                        Spacer(Modifier.height(16.dp))

                        ProviderRegistry.PRESETS.filter { it.id != "siliconflow" }.forEach { preset ->
                            ProviderSmallCard(preset = preset, onClick = { selectedPreset = preset })
                            Spacer(Modifier.height(10.dp))
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { selectedPreset = null; apiKey = ""; errorMsg = null }) {
                                Icon(Icons.Default.ArrowBack, "\u8fd4\u56de")
                            }
                            Text("\u914d\u7f6e " + selectedPreset!!.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(16.dp))

                        Surface(shape = RoundedCornerShape(16.dp), color = PrimaryContainer, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(selectedPreset!!.description, fontSize = 14.sp, color = Primary)
                                Spacer(Modifier.height(4.dp))
                                Text("\u89c6\u89c9\u6a21\u578b: " + selectedPreset!!.visionModels.size + "  |  \u6587\u672c\u6a21\u578b: " + selectedPreset!!.textModels.size, fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        if (selectedPreset!!.id == "siliconflow") {
                            Spacer(Modifier.height(12.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = Warning.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, null, tint = Warning, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("\u6ce8\u518c\u540e\u5145\u503c 2 \u5143\u5373\u53ef\u4f53\u9a8c\u5168\u90e8\u529f\u80fd", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("\u514d\u8d39\u6a21\u578b\u5b8c\u5168\u591f\u7528\uff0c\u5145\u503c\u662f\u4e3a\u4e86\u89e3\u9501\u66f4\u591a\u989d\u5916\u6a21\u578b", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SF_REFERRAL))) },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp), tint = Color(0xFF6366F1))
                                Spacer(Modifier.width(6.dp))
                                Text("\u8fd8\u6ca1\u8d26\u53f7\uff1f\u70b9\u51fb\u6ce8\u518c", fontSize = 13.sp, color = Color(0xFF6366F1))
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it; errorMsg = null },
                            label = { Text("API Key") },
                            placeholder = { Text("\u8bf7\u8f93\u5165\u60a8\u7684 API Key") },
                            singleLine = true,
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            },
                            isError = errorMsg != null,
                            supportingText = errorMsg?.let { { Text(it, color = Error) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (apiKey.isBlank()) { errorMsg = "\u8bf7\u8f93\u5165 API Key"; return@Button }
                                onAddProvider(selectedPreset!!.id, apiKey)
                                onSkipToMain()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("\u5f00\u59cb\u4f7f\u7528", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("\u60a8\u7684 API Key \u4ec5\u5b58\u50a8\u5728\u672c\u5730\uff0c\u4e0d\u4f1a\u4e0a\u4f20", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProviderSmallCard(preset: AIProvider, onClick: () -> Unit) {
    val icon: ImageVector = when (preset.id) {
        "groq" -> Icons.Default.Speed
        "gemini" -> Icons.Default.Star
        "sambanova" -> Icons.Default.Memory
        else -> Icons.Default.SmartToy
    }
    val color: Color = when (preset.id) {
        "groq" -> Color(0xFFF59E0B)
        "gemini" -> Color(0xFF10B981)
        "sambanova" -> Color(0xFF3B82F6)
        else -> Primary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(preset.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(preset.description, fontSize = 12.sp, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (preset.visionModels.isNotEmpty()) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Info.copy(alpha = 0.08f)) {
                        Text("${preset.visionModels.size} \u89c6\u89c9", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = Info)
                    }
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Success.copy(alpha = 0.08f)) {
                    Text("${preset.textModels.size} \u6587\u672c", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = Success)
                }
            }
        }
    }
}
