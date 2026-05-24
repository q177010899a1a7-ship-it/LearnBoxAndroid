package com.learnbox.data.model

import java.util.UUID

data class MindMap(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val nodes: List<MindNode> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class MindNode(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val parentId: String? = null,
    val x: Float = 0f,
    val y: Float = 0f,
    val colorIndex: Int = 0,
    val isCollapsed: Boolean = false
)

fun MindMap.toNodeMap(): Map<String, MindNode> = nodes.associateBy { it.id }

fun MindMap.childrenOf(parentId: String?): List<MindNode> = nodes.filter { it.parentId == parentId }

fun MindMap.rootNode(): MindNode? = nodes.firstOrNull { it.parentId == null }