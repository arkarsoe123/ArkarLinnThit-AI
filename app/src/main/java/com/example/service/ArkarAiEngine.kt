package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.model.ActiveToolScreen
import com.example.model.AiModelChoice
import com.example.model.DynamicUiCard
import com.example.model.ChatMessage
import com.example.model.ChatSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ArkarAiEngine {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun executePrompt(
        prompt: String,
        modelChoice: AiModelChoice,
        customApiKey: String = "",
        customEndpoint: String = "",
        modelName: String = "",
        enabledTools: Set<String> = ToolRegistry.all.map { it.id }.toSet(),
        conversationHistory: List<ChatMessage> = emptyList()
    ): AiExecutionResult = withContext(Dispatchers.IO) {
        val offline = MiniOfflineAi.processQuery(prompt)
        val detected = offline.toolToOpen
        if (detected != null && detected != ActiveToolScreen.DYNAMIC_GENERATED_UI) {
            val def = ToolRegistry.forScreen(detected)
            if (def != null && !enabledTools.contains(def.id)) {
                return@withContext AiExecutionResult(
                    responseText = "ဒီ Tool ကို Settings → Capabilities မှာ ပိတ်ထားပါတယ်။ လိုအပ်ရင် ပြန်ဖွင့်ပေးပါခင်ဗျာ။"
                )
            }
            return@withContext offline.copy(dynamicCards = cardForTool(detected, prompt))
        }

        if (modelChoice == AiModelChoice.MINI_OFFLINE_AI) {
            return@withContext offline.copy(dynamicCards = cardForPrompt(prompt))
        }

        try {
            val result = when (modelChoice) {
                AiModelChoice.GEMINI, AiModelChoice.GEMINI_3_5_FLASH, AiModelChoice.GEMINI_3_1_PRO -> callGemini(
                    prompt, customApiKey.ifBlank { buildConfigKey() }, modelName.ifBlank {
                        if (modelChoice == AiModelChoice.GEMINI_3_1_PRO) "gemini-3.1-pro" else "gemini-3.5-flash"
                    }, conversationHistory
                )
                AiModelChoice.ANTHROPIC -> callAnthropic(prompt, customApiKey, customEndpoint, modelName, conversationHistory)
                AiModelChoice.OPENAI_COMPATIBLE, AiModelChoice.LOCALHOST_MODEL, AiModelChoice.CUSTOM_API -> callOpenAiCompatible(
                    prompt,
                    customApiKey,
                    customEndpoint.ifBlank { if (modelChoice == AiModelChoice.LOCALHOST_MODEL) "http://localhost:11434/v1/chat/completions" else "https://api.openai.com/v1/chat/completions" },
                    modelName.ifBlank { if (modelChoice == AiModelChoice.LOCALHOST_MODEL) "llama3.2" else "gpt-4o-mini" },
                    conversationHistory
                )
                AiModelChoice.MINI_OFFLINE_AI -> offline
            }
            if (result.responseText.isNotBlank()) {
                return@withContext result.copy(
                    toolToOpen = result.toolToOpen ?: detectToolFromText(prompt + " " + result.responseText),
                    dynamicCards = result.dynamicCards ?: cardForPrompt(prompt)
                )
            }
            offline
        } catch (e: Exception) {
            Log.e("ArkarAiEngine", "Provider call failed: ${e.message}")
            offline.copy(responseText = offline.responseText + "\n\nCloud model ကို ချိတ်ဆက်မရသဖြင့် Mini AI ဖြင့် ဆက်ဖြေထားပါတယ်။")
        }
    }

    private fun buildConfigKey(): String = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

    private fun systemPrompt() = """
You are Arkar Lin Thit AI, a friendly Myanmar Android assistant. Reply primarily in Burmese.
You can answer normally and may suggest one UI card when useful. Never invent that a device action was completed.
For device actions, the Android app handles permissions and execution. Prefer concise answers.
""".trimIndent()

    private fun callGemini(prompt: String, key: String, model: String, history: List<ChatMessage>): AiExecutionResult {
        if (key.isBlank() || key.startsWith("MY_")) return AiExecutionResult("")
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"
        val body = JSONObject().apply {
            put("systemInstruction", JSONObject().apply { put("parts", JSONArray().put(JSONObject().put("text", systemPrompt()))) })
            put("contents", JSONArray().apply {
                history.takeLast(12).forEach { message ->
                    put(JSONObject().apply {
                        put("role", if (message.sender == ChatSender.AI) "model" else "user")
                        put("parts", JSONArray().put(JSONObject().put("text", message.text)))
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                })
            })
        }
        val response = request(url, body, emptyMap())
        if (!response.first) return AiExecutionResult("")
        val root = JSONObject(response.second)
        val text = root.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
        return AiExecutionResult(text)
    }

    private fun callOpenAiCompatible(prompt: String, key: String, endpoint: String, model: String, history: List<ChatMessage>): AiExecutionResult {
        if (key.isBlank() && endpoint.contains("api.openai.com")) return AiExecutionResult("")
        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt()))
                history.takeLast(12).forEach { message ->
                    put(JSONObject().put(
                        "role", if (message.sender == ChatSender.AI) "assistant" else "user"
                    ).put("content", message.text))
                }
                put(JSONObject().put("role", "user").put("content", prompt))
            })
        }
        val headers = if (key.isBlank()) emptyMap() else mapOf("Authorization" to "Bearer $key")
        val response = request(endpoint, body, headers)
        if (!response.first) return AiExecutionResult("")
        val root = JSONObject(response.second)
        val text = root.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content").orEmpty()
        return AiExecutionResult(text)
    }

    private fun callAnthropic(prompt: String, key: String, endpoint: String, model: String, history: List<ChatMessage>): AiExecutionResult {
        if (key.isBlank()) return AiExecutionResult("")
        val url = endpoint.ifBlank { "https://api.anthropic.com/v1/messages" }
        val body = JSONObject().apply {
            put("model", model.ifBlank { "claude-3-5-sonnet-latest" })
            put("max_tokens", 2048)
            put("system", systemPrompt())
            put("messages", JSONArray().apply {
                history.takeLast(12).forEach { message ->
                    put(JSONObject().put(
                        "role", if (message.sender == ChatSender.AI) "assistant" else "user"
                    ).put("content", message.text))
                }
                put(JSONObject().put("role", "user").put("content", prompt))
            })
        }
        val headers = mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01")
        val response = request(url, body, headers)
        if (!response.first) return AiExecutionResult("")
        val root = JSONObject(response.second)
        val text = root.optJSONArray("content")?.optJSONObject(0)?.optString("text").orEmpty()
        return AiExecutionResult(text)
    }

    private fun request(url: String, body: JSONObject, headers: Map<String, String>): Pair<Boolean, String> {
        val builder = Request.Builder().url(url).post(body.toString().toRequestBody("application/json".toMediaType()))
        headers.forEach { (k, v) -> builder.addHeader(k, v) }
        httpClient.newCall(builder.build()).execute().use { response ->
            val text = response.body?.string().orEmpty()
            return response.isSuccessful to text
        }
    }

    private fun detectToolFromText(text: String): ActiveToolScreen? {
        val lower = text.lowercase()
        return when {
            lower.contains("setting") || lower.contains("ပြင်ဆင်") -> ActiveToolScreen.SETTINGS
            lower.contains("flashlight") || lower.contains("မီး") -> ActiveToolScreen.FLASHLIGHT
            lower.contains("camera") || lower.contains("ကင်မရာ") -> ActiveToolScreen.CAMERA
            lower.contains("music") || lower.contains("သီချင်း") -> ActiveToolScreen.MUSIC_PLAYER
            lower.contains("slideshow") || lower.contains("ဓာတ်ပုံ") -> ActiveToolScreen.PHOTO_SLIDESHOW
            lower.contains("timer") || lower.contains("တိုင်မာ") -> ActiveToolScreen.TIMER_STOPWATCH
            lower.contains("call") || lower.contains("ဖုန်းခေါ်") -> ActiveToolScreen.PHONE_CALL
            lower.contains("note") || lower.contains("မှတ်စု") -> ActiveToolScreen.NOTES
            lower.contains("sheet") || lower.contains("စာရင်း") -> ActiveToolScreen.SPREADSHEET
            lower.contains("code") || lower.contains("ကုဒ်") -> ActiveToolScreen.CODE_EDITOR
            lower.contains("automation") || lower.contains("အလိုအလျောက်") -> ActiveToolScreen.SMART_AUTOMATION
            else -> null
        }
    }

    private fun cardForTool(tool: ActiveToolScreen, prompt: String): List<DynamicUiCard> = listOf(
        DynamicUiCard(
            cardType = "TOOL_RESULT",
            title = ToolRegistry.forScreen(tool)?.title ?: tool.titleEn,
            subtitle = "AI က command ကို နားလည်ပြီး သက်ဆိုင်ရာ UI ကို ပြင်ဆင်ထားပါတယ်",
            primaryMetric = "READY",
            secondaryMetric = tool.titleEn,
            statusText = prompt.take(80),
            items = listOf("Tool: ${ToolRegistry.forScreen(tool)?.id ?: tool.name}", "User command ဖြင့် စတင်ထားသည်"),
            accentHex = 0xFF5B8CFF
        )
    )

    private fun cardForPrompt(prompt: String): List<DynamicUiCard> {
        val lower = prompt.lowercase()
        return when {
            Regex("[0-9]+\\s*[+\\-*/xX]\\s*[0-9]+").containsMatchIn(prompt) -> listOf(
                DynamicUiCard(cardType = "CALCULATOR", title = "တွက်ချက်မှု", subtitle = "Mini AI calculator", primaryMetric = "READY", secondaryMetric = "Math", statusText = prompt.take(60), items = emptyList(), accentHex = 0xFF8B5CF6)
            )
            lower.contains("todo") || lower.contains("လုပ်စရာ") || lower.contains("စာရင်း") -> listOf(
                DynamicUiCard(cardType = "TODO", title = "လုပ်စရာစာရင်း", subtitle = "AI-generated task card", primaryMetric = "DRAFT", secondaryMetric = "Task", statusText = "Save နှိပ်ပြီး သိမ်းနိုင်ပါတယ်", items = listOf(prompt.take(100)), accentHex = 0xFF10B981)
            )
            else -> emptyList()
        }
    }
}
