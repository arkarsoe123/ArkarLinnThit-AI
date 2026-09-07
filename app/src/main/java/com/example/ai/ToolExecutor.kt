package com.example.ai

import android.content.Context
import com.example.service.SystemHardwareManager
import com.example.service.ToolRegistry

/**
 * Central safety-aware tool boundary. UI/view-model code should never execute
 * arbitrary AI text as an Android command.
 */
class ToolExecutor(private val context: Context) {
    private val hardware = SystemHardwareManager(context)

    fun canExecute(toolId: String, enabledTools: Set<String>): Boolean =
        ToolRegistry.all.any { it.id == toolId && enabledTools.contains(toolId) }

    fun execute(request: ToolRequest, enabledTools: Set<String>, confirmed: Boolean = false): ToolResult {
        val definition = ToolRegistry.all.firstOrNull { it.id == request.toolId }
            ?: return ToolResult(false, "Unknown tool")
        if (!enabledTools.contains(definition.id)) return ToolResult(false, "Tool is disabled")
        if ((definition.sensitive || request.requiresConfirmation) && !confirmed) {
            return ToolResult(false, "Confirmation required")
        }
        return when (request.action) {
            "TORCH_ON" -> ToolResult(hardware.setTorch(true), "Flashlight on")
            "TORCH_OFF" -> ToolResult(hardware.setTorch(false), "Flashlight off")
            "TORCH_TOGGLE" -> ToolResult(hardware.toggleTorch(), "Flashlight toggled")
            else -> ToolResult(true, "Ready", definition.screen.name)
        }
    }
}
