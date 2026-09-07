package com.example.model

enum class ChatSender {
    USER,
    AI,
    SYSTEM
}

enum class MascotEmotion {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXCITED,
    SUCCESS,
    ERROR
}

enum class ActiveToolScreen(val titleMy: String, val titleEn: String) {
    HOME("ပင်မ AI ဦးနှောက်", "AI Brain Hub"),
    FLASHLIGHT("ဖုန်းမီး", "Flashlight"),
    CAMERA("ကင်မရာ", "Camera"),
    WIFI_SYSTEM("စနစ် & ဝိုင်ဖိုင်", "System & Wi-Fi"),
    MUSIC_PLAYER("သီချင်းဖွင့်စက်", "Music Player"),
    PHOTO_SLIDESHOW("ဓာတ်ပုံ ဆလိုက်ရှိုး", "Photo Slideshow"),
    TIMER_STOPWATCH("တိုင်မာ & နာရီ", "Timer & Stopwatch"),
    PHONE_CALL("ဖုန်းခေါ်ဆိုမှု", "Phone Dialer"),
    NOTES("မှတ်စုများ", "Smart Notes"),
    SPREADSHEET("စာရင်းဇယား", "Spreadsheet"),
    CODE_EDITOR("AI Code Editor", "Code Studio"),
    SMART_AUTOMATION("စမတ် အလိုအလျောက်စနစ်", "Smart Automation"),
    DYNAMIC_GENERATED_UI("AI ဖန်တီးထားသော UI", "AI Generated UI"),
    AI_SKILLS("99+ AI Skills", "AI Skills Library"),
    DEVELOPER_SUPPORT("Developer & Support", "Developer & Support"),
    SETTINGS("စနစ်ပြင်ဆင်မှု", "Settings")
}

enum class AiModelChoice(val displayName: String, val description: String) {
    MINI_OFFLINE_AI("Built-in Mini AI · Offline", "အင်တာနက်မလိုဘဲ အခြေခံ command များအတွက်"),
    GEMINI("Gemini", "Google Gemini API · ကိုယ်ပိုင် API key ဖြင့်"),
    OPENAI_COMPATIBLE("OpenAI-compatible", "OpenAI / compatible gateways / many providers"),
    ANTHROPIC("Anthropic", "Claude API · ကိုယ်ပိုင် API key ဖြင့်"),
    LOCALHOST_MODEL("Localhost", "Ollama / LM Studio / local OpenAI-compatible server"),
    CUSTOM_API("Custom", "ကိုယ်ပိုင် endpoint + model ကို သတ်မှတ်ရန်"),
    // Legacy aliases retained for backward compatibility with older project code.
    GEMINI_3_5_FLASH("Gemini (legacy)", "Gemini provider"),
    GEMINI_3_1_PRO("Gemini Pro (legacy)", "Gemini provider")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolTriggered: ActiveToolScreen? = null,
    val dynamicUiType: String? = null
)

data class CodeFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val language: String,
    val content: String,
    val path: String = "src/$name",
    val isModified: Boolean = false
)

data class NoteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val colorHex: Long = 0xFF0284C7,
    val dateText: String,
    val tag: String = "အထွေထွေ"
)

data class AutomationRoutine(
    val id: String,
    val title: String,
    val description: String,
    val steps: List<String>,
    val targetTool: ActiveToolScreen,
    val isEnabled: Boolean = true
)

data class DynamicUiCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardType: String,
    val title: String,
    val subtitle: String,
    val primaryMetric: String,
    val secondaryMetric: String,
    val statusText: String,
    val items: List<String> = emptyList(),
    val accentHex: Long = 0xFF00F0FF
)
