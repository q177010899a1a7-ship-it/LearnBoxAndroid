package com.learnbox.ui.mindmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.learnbox.data.model.MindNode
import com.learnbox.data.model.MindMap
import com.learnbox.ui.theme.*
import kotlin.math.abs
import kotlin.math.sqrt

private const val NODE_WIDTH = 140f
private const val NODE_HEIGHT = 48f
private const val H_GAP = 80f
private const val V_GAP = 20f
private const val ROOT_WIDTH = 180f
private const val ROOT_HEIGHT = 56f

@Composable
fun MindMapCanvas(
    mindMap: MindMap,
    onNodeClick: (MindNode) -> Unit,
    onNodeDrag: (MindNode, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var draggedNode by remember { mutableStateOf<MindNode?>(null) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    var dragStartY by remember { mutableFloatStateOf(0f) }

    val textMeasurer = rememberTextMeasurer()
    val nodePositions = remember(mindMap.nodes) { calculateLayout(mindMap) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.3f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            translate(offsetX, offsetY) {
                scale(scale) {
                    // Draw connections
                    mindMap.nodes.forEach { node ->
                        if (node.parentId != null) {
                            val parentPos = nodePositions[node.parentId]
                            val childPos = nodePositions[node.id]
                            if (parentPos != null && childPos != null) {
                                drawConnection(Offset(parentPos.x, parentPos.y), Offset(childPos.x, childPos.y), node.colorIndex)
                            }
                        }
                    }
                    // Draw nodes
                    mindMap.nodes.forEach { node ->
                        val pos = nodePositions[node.id] ?: return@forEach
                        val isRoot = node.parentId == null
                        val w = if (isRoot) ROOT_WIDTH else NODE_WIDTH
                        val h = if (isRoot) ROOT_HEIGHT else NODE_HEIGHT
                        val color = MindNodeColors[node.colorIndex.coerceIn(0, MindNodeColors.size - 1)]
                        // Shadow
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.06f),
                            topLeft = Offset(pos.x - w/2 + 2, pos.y - h/2 + 2),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        // Node background
                        drawRoundRect(
                            color = if (isRoot) color else color.copy(alpha = 0.1f),
                            topLeft = Offset(pos.x - w/2, pos.y - h/2),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        // Border
                        drawRoundRect(
                            color = color.copy(alpha = if (isRoot) 1f else 0.3f),
                            topLeft = Offset(pos.x - w/2, pos.y - h/2),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = if (isRoot) 2f else 1f)
                        )
                        // Text
                        val textStyle = TextStyle(
                            color = if (isRoot) Color.White else TextPrimary,
                            fontSize = if (isRoot) 14.sp else 12.sp,
                            fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Medium
                        )
                        val textResult = textMeasurer.measure(
                            text = AnnotatedString(node.text.ifEmpty { "新节点" }),
                            style = textStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            constraints = Constraints(maxWidth = (w - 24).toInt())
                        )
                        drawText(
                            textResult,
                            topLeft = Offset(pos.x - textResult.size.width / 2, pos.y - textResult.size.height / 2)
                        )
                    }
                }
            }
        }
        // Touch handler for node clicks
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(mindMap.nodes, scale, offsetX, offsetY) {
                detectTapGestures { offset ->
                    val worldX = (offset.x - offsetX) / scale
                    val worldY = (offset.y - offsetY) / scale
                    mindMap.nodes.forEach { node ->
                        val pos = nodePositions[node.id] ?: return@forEach
                        val w = if (node.parentId == null) ROOT_WIDTH else NODE_WIDTH
                        val h = if (node.parentId == null) ROOT_HEIGHT else NODE_HEIGHT
                        if (worldX in (pos.x - w/2)..(pos.x + w/2) &&
                            worldY in (pos.y - h/2)..(pos.y + h/2)) {
                            onNodeClick(node)
                            return@detectTapGestures
                        }
                    }
                }
            }
        ) {}
    }
}

private fun DrawScope.drawConnection(parent: Offset, child: Offset, colorIndex: Int) {
    val color = MindNodeColors[(colorIndex.coerceIn(0, MindNodeColors.size - 1))]
    val midX = (parent.x + child.x) / 2
    val path = Path().apply {
        moveTo(parent.x, parent.y)
        cubicTo(midX, parent.y, midX, child.y, child.x, child.y)
    }
    drawPath(path, color.copy(alpha = 0.4f), style = Stroke(width = 2f))
}

data class NodePos(val x: Float, val y: Float)

fun calculateLayout(mindMap: MindMap): Map<String, NodePos> {
    val positions = mutableMapOf<String, NodePos>()
    val root = mindMap.nodes.firstOrNull { it.parentId == null } ?: return positions
    val centerX = 400f
    val centerY = 300f
    positions[root.id] = NodePos(centerX, centerY)
    fun layoutChildren(parentId: String, px: Float, py: Float, depth: Int, index: Int, total: Int) {
        val children = mindMap.nodes.filter { it.parentId == parentId && !it.isCollapsed }
        if (children.isEmpty()) return
        val totalHeight = children.size * (NODE_HEIGHT + V_GAP) - V_GAP
        var startY = py - totalHeight / 2
        val nx = px + (if (depth == 0) ROOT_WIDTH / 2 else NODE_WIDTH / 2) + H_GAP + NODE_WIDTH / 2
        children.forEachIndexed { i, child ->
            val ny = startY + i * (NODE_HEIGHT + V_GAP) + NODE_HEIGHT / 2
            positions[child.id] = NodePos(nx, ny)
            layoutChildren(child.id, nx, ny, depth + 1, i, children.size)
        }
    }
    layoutChildren(root.id, centerX, centerY, 0, 0, 1)
    return positions
}