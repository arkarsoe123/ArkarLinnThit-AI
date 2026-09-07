package com.example.ai

/**
 * Safe, serialisable description of an action requested by the AI.
 * Device-side execution remains under Android permission/confirmation rules.
 */
data class ToolRequest(
    val toolId: String,
    val action: String,
    val arguments: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false
)

data class ToolResult(
    val success: Boolean,
    val message: String,
    val openScreen: String? = null
)
