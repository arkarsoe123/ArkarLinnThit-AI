package com.example.service

import com.example.model.ActiveToolScreen

data class ToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val screen: ActiveToolScreen,
    val sensitive: Boolean = false,
    val permission: String? = null
)

object ToolRegistry {
    val all: List<ToolDefinition> = listOf(
        ToolDefinition("flashlight", "ဖုန်းမီး", "ဖုန်းမီး ဖွင့်/ပိတ်", ActiveToolScreen.FLASHLIGHT),
        ToolDefinition("camera", "ကင်မရာ", "ကင်မရာ workspace", ActiveToolScreen.CAMERA, sensitive = true, permission = "CAMERA"),
        ToolDefinition("wifi", "Wi‑Fi & System", "Wi‑Fi / system shortcuts", ActiveToolScreen.WIFI_SYSTEM),
        ToolDefinition("music", "Music", "Music player", ActiveToolScreen.MUSIC_PLAYER),
        ToolDefinition("gallery", "Gallery", "Photo slideshow", ActiveToolScreen.PHOTO_SLIDESHOW, permission = "READ_MEDIA"),
        ToolDefinition("timer", "Timer", "Timer & stopwatch", ActiveToolScreen.TIMER_STOPWATCH),
        ToolDefinition("phone", "Phone", "Phone dialer", ActiveToolScreen.PHONE_CALL, sensitive = true, permission = "PHONE"),
        ToolDefinition("notes", "Notes", "Smart notes", ActiveToolScreen.NOTES),
        ToolDefinition("spreadsheet", "Spreadsheet", "Tables and calculations", ActiveToolScreen.SPREADSHEET),
        ToolDefinition("code", "Code Studio", "Code editor and terminal", ActiveToolScreen.CODE_EDITOR),
        ToolDefinition("automation", "Automation", "Routines and automation", ActiveToolScreen.SMART_AUTOMATION),
        ToolDefinition("dynamic_ui", "Generated UI", "AI result cards and dashboards", ActiveToolScreen.DYNAMIC_GENERATED_UI)
    )

    fun forScreen(screen: ActiveToolScreen): ToolDefinition? = all.firstOrNull { it.screen == screen }
}
