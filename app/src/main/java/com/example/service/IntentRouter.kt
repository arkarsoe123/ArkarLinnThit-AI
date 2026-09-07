package com.example.service

import com.example.model.ActiveToolScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small deterministic intent router used before a cloud model.
 * It turns common Burmese/English commands into trusted app actions.
 */
object IntentRouter {
    data class Intent(
        val screen: ActiveToolScreen? = null,
        val action: String? = null,
        val response: String
    )

    fun route(raw: String): Intent? {
        val original = raw.trim()
        if (original.isBlank()) return null
        val p = normalize(original)

        if (p == "/cmd help" || p == "/cmd" || p == "help") {
            return Intent(response = help())
        }

        // Memory / notes — natural-language commands
        if (containsAny(p, "မှတ်ထား", "မှတ်ထားပေး", "သိမ်းထား", "save this", "remember this", "remember that", "save note")) {
            val content = extractMemoryContent(original)
            if (content.isNotBlank()) {
                val title = content.take(42).replace(Regex("\\s+"), " ").trim()
                val payload = encodeActionPayload(title, content)
                return Intent(ActiveToolScreen.NOTES, "SAVE_NOTE:$payload", "မှတ်ထားပေးလိုက်ပါပြီခင်ဗျာ။ နောက်မှ \"ရှာပေး\" လို့ပြောရင် ပြန်ရှာပေးနိုင်ပါတယ်။")
            }
        }
        if (containsAny(p, "ရှာပေး", "ရှာပေးပါ", "အရင်ပြောထားတာရှာ", "မှတ်ထားတာရှာ", "search memory", "search notes", "find note", "find memory")) {
            val query = extractSearchQuery(original)
            if (query.isNotBlank()) {
                return Intent(ActiveToolScreen.NOTES, "SEARCH_MEMORY:${encodePayload(query)}", "မှတ်ဉာဏ်နဲ့ မှတ်စုတွေထဲမှာ ရှာပေးနေပါတယ်ခင်ဗျာ။")
            }
            return Intent(ActiveToolScreen.NOTES, "OPEN_NOTES", "ဘာကိုရှာရမလဲ ပြောပေးပါခင်ဗျာ။ ဥပမာ — \"အရင်ပြောထားတာထဲက project ရှာ\"။")
        }

        // Quick device time/date without opening a tool.
        if (containsAny(p, "အခုဘယ်နှစ်နာရီ", "အချိန်ဘယ်လောက်", "what time", "current time", "အခုအချိန်")) {
            val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            return Intent(response = "အခုအချိန် $now ပါခင်ဗျာ။")
        }
        if (containsAny(p, "ဒီနေ့ရက်စွဲ", "ဒီနေ့ဘယ်ရက်", "today date", "what date", "ဒီနေ့ရက်")) {
            val now = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            return Intent(response = "ဒီနေ့ရက်စွဲက $now ပါခင်ဗျာ။")
        }

        // Home / navigation
        if (containsAny(p, "go home", "home", "chat home", "ပင်မ", "မူလစာမျက်နှာ")) {
            return Intent(ActiveToolScreen.HOME, "NAV_HOME", "Chat Home ကို ပြန်ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")
        }
        if (containsAny(p, "setting", "settings", "config", "စနစ်ပြင်ဆင်", "ချိန်ညှိချက်")) {
            return Intent(ActiveToolScreen.SETTINGS, "NAV_SETTINGS", "Settings ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")
        }
        if (containsAny(p, "tools", "tool", "capabilities", "လုပ်ဆောင်ချက်များ")) {
            return Intent(ActiveToolScreen.SMART_AUTOMATION, "NAV_TOOLS", "Tools ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")
        }

        // Flashlight
        if (containsAny(p, "flashlight", "torch", "ဖုန်းမီး", "မီး")) {
            val action = when {
                containsAny(p, "off", "ပိတ်", "ငြိမ်း") -> "TORCH_OFF"
                containsAny(p, "toggle", "ပြောင်း", "ဖွင့်ပိတ်") -> "TORCH_TOGGLE"
                else -> "TORCH_ON"
            }
            val text = if (action == "TORCH_OFF") "ဖုန်းမီး ပိတ်ပေးလိုက်ပါပြီခင်ဗျာ။" else "ဖုန်းမီး ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။"
            return Intent(ActiveToolScreen.FLASHLIGHT, action, text)
        }

        // Camera
        if (containsAny(p, "camera", "ကင်မရာ", "ဓာတ်ပုံရိုက်", "ဓါတ်ပုံရိုက်")) {
            return Intent(ActiveToolScreen.CAMERA, "OPEN_CAMERA", "ကင်မရာကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။ Permission လိုအပ်ရင် Android က တောင်းပါလိမ့်မယ်။")
        }

        // Gallery / slideshow
        if (containsAny(p, "gallery", "photos", "photo gallery", "slideshow", "ဆလိုက်ရှိုး", "ဓာတ်ပုံတွေ", "ဓာတ်ပုံများ")) {
            return Intent(ActiveToolScreen.PHOTO_SLIDESHOW, "OPEN_GALLERY", "ဖုန်းထဲက တကယ့်ဓာတ်ပုံတွေ ရွေးနိုင်တဲ့ Gallery Slideshow ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")
        }

        // Music
        if (containsAny(p, "music", "song", "audio", "သီချင်း", "တေးဂီတ")) {
            val action = when {
                containsAny(p, "next", "နောက်သီချင်း", "နောက်တစ်ပုဒ်") -> "MUSIC_NEXT"
                containsAny(p, "previous", "prev", "အရင်သီချင်း", "ရှေ့သီချင်း") -> "MUSIC_PREV"
                containsAny(p, "pause", "ရပ်", "ခဏရပ်") -> "MUSIC_PAUSE"
                containsAny(p, "stop", "ပိတ်", "ရပ်လိုက်") -> "MUSIC_STOP"
                else -> "PLAY_MUSIC"
            }
            return Intent(ActiveToolScreen.MUSIC_PLAYER, action, when (action) {
                "MUSIC_NEXT" -> "နောက်သီချင်းကို ပြောင်းပေးလိုက်ပါပြီခင်ဗျာ။"
                "MUSIC_PREV" -> "အရင်သီချင်းကို ပြန်ပြောင်းပေးလိုက်ပါပြီခင်ဗျာ။"
                "MUSIC_PAUSE" -> "သီချင်းကို ခဏရပ်ပေးလိုက်ပါပြီခင်ဗျာ။"
                "MUSIC_STOP" -> "သီချင်းကို ရပ်ပေးလိုက်ပါပြီခင်ဗျာ။"
                else -> "Music Player ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။"
            })
        }

        // Timer / stopwatch with duration parsing
        if (containsAny(p, "timer", "countdown", "တိုင်မာ", "အချိန်မှတ်")) {
            if (containsAny(p, "stop", "cancel", "ပိတ်", "ရပ်")) {
                return Intent(ActiveToolScreen.TIMER_STOPWATCH, "STOP_TIMER", "တိုင်မာကို ရပ်ပေးလိုက်ပါပြီခင်ဗျာ။")
            }
            val seconds = parseDurationSeconds(p)
            return Intent(
                ActiveToolScreen.TIMER_STOPWATCH,
                seconds?.let { "START_TIMER:$it" } ?: "OPEN_TIMER",
                if (seconds != null) "${formatDuration(seconds)} တိုင်မာကို စတင်ပေးလိုက်ပါပြီခင်ဗျာ။" else "Timer ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။ အချိန်သတ်မှတ်ပြီး Start နှိပ်နိုင်ပါတယ်။"
            )
        }

        if (containsAny(p, "stopwatch", "စက္ကန့်နာရီ")) {
            return Intent(ActiveToolScreen.TIMER_STOPWATCH, "OPEN_STOPWATCH", "Stopwatch ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")
        }

        // Other tools
        tool(p, listOf("phone", "call", "dial", "ဖုန်းခေါ်", "ဖုန်းဆက်"), ActiveToolScreen.PHONE_CALL, "ဖုန်းခေါ်ဆိုမှု စနစ်ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        tool(p, listOf("note", "notes", "memo", "မှတ်စု", "မှတ်ထား"), ActiveToolScreen.NOTES, "Smart Notes ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        tool(p, listOf("spreadsheet", "excel", "sheet", "table", "စာရင်းဇယား"), ActiveToolScreen.SPREADSHEET, "Spreadsheet ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        tool(p, listOf("code", "programming", "python", "kotlin", "terminal", "ကုဒ်"), ActiveToolScreen.CODE_EDITOR, "AI Code Studio ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        tool(p, listOf("wifi", "ဝိုင်ဖိုင်"), ActiveToolScreen.WIFI_SYSTEM, "Wi‑Fi & System ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        tool(p, listOf("automation", "routine", "အလိုအလျောက်", "လုပ်ငန်းစဉ်"), ActiveToolScreen.SMART_AUTOMATION, "Smart Automation ကို ဖွင့်ပေးလိုက်ပါပြီခင်ဗျာ။")?.let { return it }
        if (containsAny(p, "generate ui", "dashboard", "dynamic ui", "ui ဖန်တီး")) {
            return Intent(ActiveToolScreen.DYNAMIC_GENERATED_UI, "GENERATE_UI", "Dynamic UI အသစ်ကို ဖန်တီးပေးလိုက်ပါပြီခင်ဗျာ။")
        }

        return null
    }

    private fun tool(p: String, words: List<String>, screen: ActiveToolScreen, response: String): Intent? =
        if (words.any { p.contains(it) }) Intent(screen, "OPEN_${screen.name}", response) else null

    private fun containsAny(text: String, vararg words: String): Boolean = words.any { text.contains(it) }

    private fun extractMemoryContent(original: String): String {
        val cleaned = original
            .replace(Regex("^(အာကာလင်းသစ်\\s*)?(ဒီဟာကို\\s*)?"), "")
            .replace(Regex("^(မှတ်ထား(?:ပေး)?|သိမ်းထား(?:ပေး)?|save this|remember this|remember that|save note)\\s*[:：,-]?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+(နော်|ပါ|ပေးပါ|ပေးနော်)$"), "")
            .trim()
        return cleaned
    }

    private fun extractSearchQuery(original: String): String {
        return original
            .replace(Regex("^(အာကာလင်းသစ်\\s*)?"), "")
            .replace(Regex("(အရင်ပြောထားတာ|မှတ်ထားတာ|memory|notes?|မှတ်စု)(ထဲက|ထဲမှာ)?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(ရှာပေး(?:ပါ)?|search memory|search notes|find note|find memory)\\s*[:：,-]?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+(ကို|နဲ့|ပါ|ပေးပါ)$"), "")
            .trim()
    }

    private fun encodeActionPayload(text: String, content: String): String =
        "${encodePayload(title = text)}|${encodePayload(content)}"

    private fun encodePayload(title: String): String =
        android.util.Base64.encodeToString(title.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

    private fun normalize(input: String): String {
        val myDigits = "၀၁၂၃၄၅၆၇၈၉"
        val ascii = "0123456789"
        var out = input.lowercase().trim()
        myDigits.forEachIndexed { i, c -> out = out.replace(c, ascii[i]) }
        return out.replace(Regex("\\s+"), " ")
    }

    private fun parseDurationSeconds(text: String): Int? {
        val number = Regex("(\\d+(?:\\.\\d+)?)").find(text)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: return null
        val multiplier = when {
            containsAny(text, "hour", "hours", "နာရီ") -> 3600.0
            containsAny(text, "minute", "minutes", "min", "မိနစ်") -> 60.0
            else -> 1.0
        }
        return (number * multiplier).toInt().coerceIn(1, 86400)
    }

    private fun formatDuration(seconds: Int): String = when {
        seconds >= 3600 -> "${seconds / 3600} နာရီ ${seconds % 3600 / 60} မိနစ်"
        seconds >= 60 -> "${seconds / 60} မိနစ် ${seconds % 60} စက္ကန့်"
        else -> "$seconds စက္ကန့်"
    }

    fun help(): String = """အသုံးပြုနိုင်တာတွေ:
• ဖုန်းမီးဖွင့် / မီးပိတ်
• ကင်မရာဖွင့် / ဓာတ်ပုံရိုက်
• သီချင်းဖွင့် / ရပ် / နောက်တစ်ပုဒ်
• ၅ မိနစ် timer / 30 seconds timer
• Gallery / slideshow
• Notes / Spreadsheet / Code / Wi‑Fi / Settings
• \"ဒီဟာကို မှတ်ထား — ...\"
• \"အရင်ပြောထားတာထဲက project ရှာ\"
• \"အခုဘယ်နှစ်နာရီလဲ\"
• /cmd help"""
}
