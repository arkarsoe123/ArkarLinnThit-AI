package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ActiveToolScreen
import com.example.model.AiModelChoice
import com.example.model.AutomationRoutine
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.CodeFile
import com.example.model.DynamicUiCard
import com.example.model.MascotEmotion
import com.example.model.NoteItem
import com.example.service.ArkarAiEngine
import com.example.service.MiniOfflineAi
import com.example.service.MusicPlaybackService
import com.example.service.SystemHardwareManager
import com.example.service.VoiceAssistantManager
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class ArkarUiState(
    val activeScreen: ActiveToolScreen = ActiveToolScreen.HOME,
    val mascotEmotion: MascotEmotion = MascotEmotion.IDLE,
    val chatMessages: List<ChatMessage> = emptyList(),
    val isListeningVoice: Boolean = false,
    val isSpeakingVoice: Boolean = false,
    val isTorchActive: Boolean = false,
    val promptInput: String = "",

    // Tool animation HUD notification
    val toolTransitionNotice: String? = null,
    val animatedToolChoice: ActiveToolScreen? = null,

    // Settings
    val aiModel: AiModelChoice = AiModelChoice.MINI_OFFLINE_AI,
    val customApiKey: String = "",
    val apiEndpoint: String = "",
    val aiModelName: String = "",
    val localhostUrl: String = "http://localhost:11434/v1/chat/completions",
    val enabledTools: Map<String, Boolean> = emptyMap(),
    val requireConfirmation: Boolean = true,
    val wakeWordEnabled: Boolean = true,
    val wakeWordPhrase: String = "အာကာလင်းသစ်",
    val voiceFeedbackEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.DARK,

    // Popup / Floating Window state
    val isFloatingChatVisible: Boolean = false,
    val isFloatingChatMinimized: Boolean = false,

    // Music Player state
    val isMusicPlaying: Boolean = false,
    val currentSongIndex: Int = 0,
    val musicProgress: Float = 0.25f,
    val musicVolume: Float = 0.8f,

    // Slideshow state
    val currentSlideIndex: Int = 0,
    val isSlideshowAutoPlay: Boolean = true,

    // Timer & Stopwatch
    val timerSecondsRemaining: Int = 180,
    val isTimerRunning: Boolean = false,
    val stopwatchTimeMs: Long = 0L,
    val isStopwatchRunning: Boolean = false,
    val stopwatchLaps: List<String> = emptyList(),

    // Phone Dialer
    val dialedNumber: String = "",

    // Notes
    val notes: List<NoteItem> = emptyList(),
    val noteSearchQuery: String = "",

    // Spreadsheet
    val spreadsheetCells: Map<String, String> = emptyMap(),
    val selectedCellKey: String = "A1",

    // Code Editor
    val codeFiles: List<CodeFile> = emptyList(),
    val activeFileIndex: Int = 0,
    val terminalLogs: String = "အာကာလင်းသစ် AI Kernel Initialized.\nWorkspace Ready.",
    val isTerminalOpen: Boolean = true,
    val recentFileNames: List<String> = emptyList(),

    // Smart Automation
    val automationRoutines: List<AutomationRoutine> = emptyList(),

    // Dynamic UI Generation
    val dynamicUiCards: List<DynamicUiCard> = emptyList()
)

class ArkarMainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ArkarUiState())
    val uiState: StateFlow<ArkarUiState> = _uiState.asStateFlow()

    private val hardwareManager = SystemHardwareManager(application)
    private val aiEngine = ArkarAiEngine()
    private val prefs = application.getSharedPreferences("arkar_ai_preferences", Context.MODE_PRIVATE)

    private var voiceManager: VoiceAssistantManager? = null
    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null
    private var slideshowJob: Job? = null
    private var musicJob: Job? = null

    init {
        initDefaultState()
        loadPreferences()
        loadChatHistory()
        loadNotes()
        initVoice()
        startSlideshowLoop()
    }

    private fun loadPreferences() {
        val enabled = com.example.service.ToolRegistry.all.associate { it.id to prefs.getBoolean("tool_${it.id}", true) }
        val model = runCatching { AiModelChoice.valueOf(prefs.getString("ai_model", AiModelChoice.MINI_OFFLINE_AI.name)!!) }.getOrDefault(AiModelChoice.MINI_OFFLINE_AI)
        _uiState.update { it.copy(
            aiModel = model,
            customApiKey = prefs.getString("api_key", "") ?: "",
            apiEndpoint = prefs.getString("endpoint", "") ?: "",
            aiModelName = prefs.getString("model_name", "") ?: "",
            enabledTools = enabled,
            requireConfirmation = prefs.getBoolean("require_confirmation", true),
            wakeWordEnabled = prefs.getBoolean("wake_word_enabled", true),
            wakeWordPhrase = prefs.getString("wake_word_phrase", "အာကာလင်းသစ်") ?: "အာကာလင်းသစ်",
            voiceFeedbackEnabled = prefs.getBoolean("voice_feedback", true),
            themeMode = runCatching { AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name) }.getOrDefault(AppThemeMode.DARK)
        ) }
    }

    private fun loadChatHistory() {
        val raw = prefs.getString("chat_history", null) ?: return
        runCatching {
            val array = JSONArray(raw)
            val messages = buildList {
                for (i in 0 until array.length()) {
                    val o = array.optJSONObject(i) ?: continue
                    val sender = runCatching { ChatSender.valueOf(o.optString("sender")) }.getOrNull() ?: continue
                    val text = o.optString("text")
                    if (text.isNotBlank()) add(ChatMessage(
                        id = o.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                        sender = sender,
                        text = text,
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
                    ))
                }
            }
            if (messages.isNotEmpty()) {
                _uiState.update { it.copy(chatMessages = messages.takeLast(100)) }
            }
        }
    }

    private fun saveChatHistory(messages: List<ChatMessage> = _uiState.value.chatMessages) {
        runCatching {
            val array = JSONArray()
            messages.takeLast(100).forEach { m ->
                array.put(JSONObject().apply {
                    put("id", m.id)
                    put("sender", m.sender.name)
                    put("text", m.text)
                    put("timestamp", m.timestamp)
                })
            }
            prefs.edit().putString("chat_history", array.toString()).apply()
        }
    }

    private fun loadNotes() {
        val raw = prefs.getString("memory_notes", null) ?: return
        runCatching {
            val array = JSONArray(raw)
            val notes = buildList {
                for (i in 0 until array.length()) {
                    val o = array.optJSONObject(i) ?: continue
                    val title = o.optString("title")
                    val content = o.optString("content")
                    if (title.isNotBlank() || content.isNotBlank()) add(NoteItem(
                        id = o.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                        title = title.ifBlank { "မှတ်စု" },
                        content = content,
                        colorHex = o.optLong("colorHex", 0xFF0284C7),
                        dateText = o.optString("dateText", ""),
                        tag = o.optString("tag", "အထွေထွေ")
                    ))
                }
            }
            if (notes.isNotEmpty()) _uiState.update { it.copy(notes = notes) }
        }
    }

    private fun saveNotes(notes: List<NoteItem> = _uiState.value.notes) {
        runCatching {
            val array = JSONArray()
            notes.take(200).forEach { n ->
                array.put(JSONObject().apply {
                    put("id", n.id); put("title", n.title); put("content", n.content)
                    put("colorHex", n.colorHex); put("dateText", n.dateText); put("tag", n.tag)
                })
            }
            prefs.edit().putString("memory_notes", array.toString()).apply()
        }
    }

    private fun initDefaultState() {
        val initialMessages = listOf(
            ChatMessage(
                sender = ChatSender.AI,
                text = "မင်္ဂလာပါခင်ဗျာ! ကျွန်တော်ကတော့ အာကာလင်းသစ် AI ဖြစ်ပါတယ်။ ဖုန်းတွင်း လုပ်ဆောင်ချက်များကို စာ သို့မဟုတ် အသံဖြင့် အချိန်မရွေး စေခိုင်းနိုင်ပါတယ်ခင်ဗျာ။ 'ဖုန်းမီးဖွင့်ပါ'၊ 'သီချင်းဖွင့်ပါ'၊ 'ကုဒ်ရေးမယ်'၊ 'setting' စသည်ဖြင့် စေခိုင်းနိုင်ပါတယ်။"
            )
        )

        val defaultNotes = listOf(
            NoteItem(
                title = "ခေတ်သစ် AI မိတ်ဆက်",
                content = "အာကာလင်းသစ် AI စနစ်သစ်တွင် Wake-word၊ စနစ်ထိန်းချုပ်မှုများ၊ Code Editor နှင့် Dynamic UI များ ပါဝင်သည်။",
                dateText = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
                tag = "အရေးကြီး"
            ),
            NoteItem(
                title = "နေ့စဉ် လုပ်ငန်းစဉ်များ",
                content = "၁။ မနက်ခင်း စနစ်လည်ပတ်မှု စစ်ဆေးခြင်း\n၂။ စာရင်းဇယား စစ်ဆေးတွက်ချက်ခြင်း\n၃။ Code Studio စမ်းသပ်ခြင်း",
                dateText = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
                tag = "အလုပ်"
            )
        )

        val defaultSheet = mutableMapOf(
            "A1" to "ခေါင်းစဉ်", "B1" to "ပမာဏ", "C1" to "နှုန်းထား", "D1" to "စုစုပေါင်း",
            "A2" to "ပရောဂျက် A", "B2" to "10", "C2" to "5000", "D2" to "50000",
            "A3" to "ပရောဂျက် B", "B3" to "25", "C3" to "3000", "D3" to "75000",
            "A4" to "ပရောဂျက် C", "B4" to "15", "C4" to "4000", "D4" to "60000",
            "A5" to "အထွေထွေ", "B5" to "5", "C5" to "2000", "D5" to "10000"
        )

        val defaultCodeFiles = listOf(
            CodeFile(
                name = "Main.kt",
                language = "kotlin",
                content = """// အာကာလင်းသစ် AI Kotlin Studio
fun main() {
    println("မင်္ဂလာပါ ခေတ်သစ် နည်းပညာ!")
    val assistant = "Arkar Lin Thit AI"
    println("Active Engine: " + assistant)
}"""
            ),
            CodeFile(
                name = "automation.py",
                language = "python",
                content = """# Python Smart Automation Script
def run_routine():
    print("Executing Arkar Smart Automation...")
    tools = ["Flashlight", "Camera", "Music", "Timer"]
    for tool in tools:
        print(f"Tool {tool} synced.")

if __name__ == '__main__':
    run_routine()"""
            ),
            CodeFile(
                name = "index.html",
                language = "html",
                content = """<!DOCTYPE html>
<html>
<head>
    <title>အာကာလင်းသစ် AI Dashboard</title>
</head>
<body style="background:#0F172A; color:#00F0FF; font-family:sans-serif;">
    <h1>Arkar Lin Thit Next-Gen Interface</h1>
    <p>Voice + Text Hardware Sync Active.</p>
</body>
</html>"""
            )
        )

        val defaultRoutines = listOf(
            AutomationRoutine(
                id = "routine_morning",
                title = "မနက်ခင်း စနစ် အလိုအလျောက် (Morning Mode)",
                description = "သီချင်းဖွင့်ခြင်း၊ အချိန်နာရီနှင့် နေ့စဉ် မှတ်စုများကို ချက်ချင်း ပြသပေးမည်။",
                steps = listOf("သီချင်းဖွင့်စက် အသင့်ပြင်ခြင်း", "အချိန်နာရီ စစ်ဆေးခြင်း", "မှတ်စုများ ပြသပေးခြင်း"),
                targetTool = ActiveToolScreen.MUSIC_PLAYER
            ),
            AutomationRoutine(
                id = "routine_focus",
                title = "အာရုံစူးစိုက်မှု စနစ် (Focus Mode)",
                description = "အသံတိတ်ခြင်း၊ Timer မိနစ် ၂၅ မိနစ် သတ်မှတ်ခြင်းနှင့် Code Editor ဖွင့်ခြင်း။",
                steps = listOf("Timer ၂၅ မိနစ် စတင်ခြင်း", "Code Editor ဖွင့်ပေးခြင်း"),
                targetTool = ActiveToolScreen.TIMER_STOPWATCH
            ),
            AutomationRoutine(
                id = "routine_night",
                title = "ညဘက် အနားယူမှု စနစ် (Night Routine)",
                description = "ဖုန်းမီး စစ်ဆေးခြင်း၊ အလင်းရောင် လျှော့ချခြင်းနှင့် အနားယူသံစဉ် ဖွင့်ခြင်း။",
                steps = listOf("ဖုန်းမီး ပိတ်ပေးခြင်း", "Dark Mode ချိန်ညှိခြင်း"),
                targetTool = ActiveToolScreen.FLASHLIGHT
            )
        )

        _uiState.update {
            it.copy(
                chatMessages = initialMessages,
                notes = defaultNotes,
                spreadsheetCells = defaultSheet,
                codeFiles = defaultCodeFiles,
                recentFileNames = listOf("Main.kt", "automation.py", "index.html"),
                automationRoutines = defaultRoutines,
                dynamicUiCards = MiniOfflineAi.generateRandomDynamicUiCards()
            )
        }
    }

    private fun initVoice() {
        voiceManager = VoiceAssistantManager(
            context = getApplication(),
            onSpeechResult = { text ->
                sendUserPrompt(text)
            },
            onWakeWordDetected = {
                triggerWakeWordReaction()
            }
        ).also { manager ->
            manager.wakeWordEnabled = _uiState.value.wakeWordEnabled
            manager.currentWakeWord = _uiState.value.wakeWordPhrase
        }
    }

    fun triggerWakeWordReaction() {
        _uiState.update { it.copy(mascotEmotion = MascotEmotion.EXCITED) }
        hardwareManager.vibrate(80)
        speakVoice("ဟုတ်ကဲ့ခင်ဗျာ! အာကာလင်းသစ် အသင့်ရှိပါတယ်ခင်ဗျာ။")
    }

    fun toggleVoiceListening() {
        val currentlyListening = _uiState.value.isListeningVoice
        if (currentlyListening) {
            voiceManager?.stopListening()
            _uiState.update { it.copy(isListeningVoice = false, mascotEmotion = MascotEmotion.IDLE) }
        } else {
            val started = voiceManager?.startListening() == true
            _uiState.update { it.copy(isListeningVoice = started, mascotEmotion = if (started) MascotEmotion.LISTENING else MascotEmotion.ERROR) }
            if (!started) {
                viewModelScope.launch {
                    delay(1800)
                    _uiState.update { it.copy(mascotEmotion = MascotEmotion.IDLE) }
                }
            }
        }
    }

    fun updatePromptInput(text: String) {
        _uiState.update { it.copy(promptInput = text) }
    }

    fun sendUserPrompt(explicitPrompt: String? = null) {
        val query = (explicitPrompt ?: _uiState.value.promptInput).trim()
        if (query.isBlank()) return

        // Add user message
        val userMsg = ChatMessage(sender = ChatSender.USER, text = query)
        _uiState.update {
            it.copy(
                promptInput = "",
                chatMessages = (it.chatMessages + userMsg).takeLast(100),
                mascotEmotion = MascotEmotion.THINKING
            )
        }
        saveChatHistory()

        viewModelScope.launch {
            val result = aiEngine.executePrompt(
                prompt = query,
                modelChoice = _uiState.value.aiModel,
                customApiKey = _uiState.value.customApiKey,
                customEndpoint = _uiState.value.apiEndpoint.ifBlank { _uiState.value.localhostUrl },
                modelName = _uiState.value.aiModelName,
                enabledTools = _uiState.value.enabledTools.filterValues { it }.keys,
                conversationHistory = _uiState.value.chatMessages.dropLast(1).takeLast(12)
            )

            // Render the model/router card first; action-specific cards can override it below.
            if (result.dynamicCards != null) {
                _uiState.update { it.copy(dynamicUiCards = result.dynamicCards) }
            }

            // Handle system action command if any
            var actionResponseOverride: String? = null
            result.actionCommand?.let { cmd ->
                when {
                    cmd == "TORCH_ON" -> setTorch(true)
                    cmd == "TORCH_OFF" -> setTorch(false)
                    cmd == "TORCH_TOGGLE" -> toggleTorch()
                    cmd == "PLAY_MUSIC" -> toggleMusicPlay(true)
                    cmd == "MUSIC_PAUSE" || cmd == "MUSIC_STOP" -> toggleMusicPlay(false)
                    cmd == "MUSIC_NEXT" -> nextMusicTrack()
                    cmd == "MUSIC_PREV" -> prevMusicTrack()
                    cmd == "START_TIMER" -> startTimer()
                    cmd == "STOP_TIMER" -> pauseTimer()
                    cmd.startsWith("START_TIMER:") -> {
                        val seconds = cmd.substringAfter(':').toIntOrNull()?.coerceIn(1, 86400)
                        if (seconds != null) {
                            resetTimer(seconds)
                            startTimer()
                        }
                    }
                    cmd.startsWith("SAVE_NOTE:") -> {
                        val payload = cmd.substringAfter("SAVE_NOTE:")
                        val parts = payload.split('|', limit = 2)
                        if (parts.size == 2) {
                            val title = decodeMemory(parts[0])
                            val content = decodeMemory(parts[1])
                            addNote(title, content, "AI Memory")
                        }
                    }
                    cmd.startsWith("SEARCH_MEMORY:") -> {
                        val query = decodeMemory(cmd.substringAfter("SEARCH_MEMORY:"))
                        val answer = searchMemory(query)
                        _uiState.update { it.copy(dynamicUiCards = listOf(
                            DynamicUiCard(
                                cardType = "MEMORY_SEARCH", title = "မှတ်ဉာဏ်ရှာဖွေမှု", subtitle = "Chat + Smart Notes",
                                primaryMetric = answer.first.toString(), secondaryMetric = "matches",
                                statusText = query.take(70), items = answer.second, accentHex = 0xFF14B8A6
                            )
                        )) }
                        actionResponseOverride = answer.third
                    }
                }
            }

            // Handle tool switching with animated HUD indicator
            result.toolToOpen?.let { tool ->
                showToolAnimationNotice(tool)
            }

            // Add AI response message
            val aiMsg = ChatMessage(
                sender = ChatSender.AI,
                text = actionResponseOverride ?: result.responseText,
                toolTriggered = result.toolToOpen
            )

            _uiState.update {
                it.copy(
                    chatMessages = (it.chatMessages + aiMsg).takeLast(100),
                    mascotEmotion = MascotEmotion.SPEAKING
                )
            }
            saveChatHistory()

            // Speak response if voice feedback is enabled
            if (_uiState.value.voiceFeedbackEnabled) {
                speakVoice(result.responseText)
            }

            delay(2500)
            if (_uiState.value.mascotEmotion == MascotEmotion.SPEAKING) {
                _uiState.update { it.copy(mascotEmotion = MascotEmotion.IDLE) }
            }
        }
    }

    private fun showToolAnimationNotice(tool: ActiveToolScreen) {
        _uiState.update {
            it.copy(
                activeScreen = tool,
                animatedToolChoice = tool,
                toolTransitionNotice = "AI မှ '${tool.titleMy}' Tool ကို ဖွင့်ပေးနေပါသည်..."
            )
        }
        hardwareManager.vibrate(40)
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(toolTransitionNotice = null, animatedToolChoice = null) }
        }
    }

    fun selectScreen(screen: ActiveToolScreen) {
        showToolAnimationNotice(screen)
    }

    // Flashlight
    fun toggleTorch() {
        val next = !_uiState.value.isTorchActive
        setTorch(next)
    }

    fun setTorch(on: Boolean) {
        hardwareManager.setTorch(on)
        _uiState.update { it.copy(isTorchActive = on) }
    }

    // System Intents
    fun openWifiSettings() = hardwareManager.openWifiSettings()
    fun openQuickShare() = hardwareManager.openQuickShare("အာကာလင်းသစ် AI ဖြင့် မျှဝေခြင်း")
    fun dialNumber(number: String) = hardwareManager.dialPhoneNumber(number)

    // Voice Feedback
    fun speakVoice(text: String) {
        _uiState.update { it.copy(isSpeakingVoice = true) }
        voiceManager?.speak(text)
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(isSpeakingVoice = false) }
        }
    }

    fun stopVoice() {
        voiceManager?.stopSpeaking()
        _uiState.update { it.copy(isSpeakingVoice = false) }
    }

    // Floating Popup Chat Box
    fun toggleFloatingChat() {
        _uiState.update { it.copy(isFloatingChatVisible = !it.isFloatingChatVisible) }
    }

    fun minimizeFloatingChat(minimize: Boolean) {
        _uiState.update { it.copy(isFloatingChatMinimized = minimize) }
    }

    // Music Player controls — delegated to the real foreground playback service.
    private fun sendMusicServiceAction(action: String) {
        val intent = android.content.Intent(getApplication(), MusicPlaybackService::class.java).setAction(action)
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= 26 && action != MusicPlaybackService.ACTION_STOP) {
                getApplication<Application>().startForegroundService(intent)
            } else {
                getApplication<Application>().startService(intent)
            }
        }
    }

    fun toggleMusicPlay(forcePlay: Boolean? = null) {
        when (forcePlay) {
            true -> sendMusicServiceAction(MusicPlaybackService.ACTION_PLAY)
            false -> sendMusicServiceAction(MusicPlaybackService.ACTION_PAUSE)
            null -> sendMusicServiceAction(MusicPlaybackService.ACTION_PLAY_PAUSE)
        }
    }

    fun nextMusicTrack() { sendMusicServiceAction(MusicPlaybackService.ACTION_NEXT) }

    fun prevMusicTrack() { sendMusicServiceAction(MusicPlaybackService.ACTION_PREVIOUS) }

    // Photo Slideshow
    private fun startSlideshowLoop() {
        slideshowJob?.cancel()
        slideshowJob = viewModelScope.launch {
            while (isActive) {
                delay(3500)
                if (_uiState.value.isSlideshowAutoPlay) {
                    _uiState.update {
                        it.copy(currentSlideIndex = (it.currentSlideIndex + 1) % 5)
                    }
                }
            }
        }
    }

    fun setSlideshowAutoPlay(enable: Boolean) {
        _uiState.update { it.copy(isSlideshowAutoPlay = enable) }
    }

    fun nextSlide() {
        _uiState.update { it.copy(currentSlideIndex = (it.currentSlideIndex + 1) % 5) }
    }

    fun prevSlide() {
        _uiState.update {
            val nextIdx = if (it.currentSlideIndex - 1 < 0) 4 else it.currentSlideIndex - 1
            it.copy(currentSlideIndex = nextIdx)
        }
    }

    // Timer & Stopwatch
    fun startTimer() {
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.isTimerRunning && _uiState.value.timerSecondsRemaining > 0) {
                delay(1000)
                _uiState.update { it.copy(timerSecondsRemaining = it.timerSecondsRemaining - 1) }
            }
            if (_uiState.value.timerSecondsRemaining == 0) {
                _uiState.update { it.copy(isTimerRunning = false) }
                hardwareManager.vibrate(500)
                speakVoice("တိုင်မာ အချိန်ပြည့်ပါပြီခင်ဗျာ!")
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isTimerRunning = false) }
        timerJob?.cancel()
    }

    fun resetTimer(seconds: Int = 180) {
        timerJob?.cancel()
        _uiState.update { it.copy(timerSecondsRemaining = seconds, isTimerRunning = false) }
    }

    fun toggleStopwatch() {
        val nextRunning = !_uiState.value.isStopwatchRunning
        _uiState.update { it.copy(isStopwatchRunning = nextRunning) }
        if (nextRunning) {
            stopwatchJob?.cancel()
            stopwatchJob = viewModelScope.launch {
                val startReal = System.currentTimeMillis() - _uiState.value.stopwatchTimeMs
                while (isActive && _uiState.value.isStopwatchRunning) {
                    delay(50)
                    val elapsed = System.currentTimeMillis() - startReal
                    _uiState.update { it.copy(stopwatchTimeMs = elapsed) }
                }
            }
        } else {
            stopwatchJob?.cancel()
        }
    }

    fun lapStopwatch() {
        val ms = _uiState.value.stopwatchTimeMs
        val formatted = formatStopwatch(ms)
        _uiState.update {
            it.copy(stopwatchLaps = it.stopwatchLaps + "Lap ${it.stopwatchLaps.size + 1}: $formatted")
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _uiState.update {
            it.copy(stopwatchTimeMs = 0L, isStopwatchRunning = false, stopwatchLaps = emptyList())
        }
    }

    private fun formatStopwatch(ms: Long): String {
        val sec = (ms / 1000) % 60
        val min = (ms / (1000 * 60)) % 60
        val hundredths = (ms % 1000) / 10
        return "%02d:%02d.%02d".format(min, sec, hundredths)
    }

    // Phone Dialer
    fun appendDialDigit(digit: String) {
        hardwareManager.vibrate(20)
        _uiState.update { it.copy(dialedNumber = it.dialedNumber + digit) }
    }

    fun deleteDialDigit() {
        _uiState.update {
            if (it.dialedNumber.isNotEmpty()) {
                it.copy(dialedNumber = it.dialedNumber.dropLast(1))
            } else it
        }
    }

    fun clearDialNumber() {
        _uiState.update { it.copy(dialedNumber = "") }
    }

    // Notes
    fun addNote(title: String, content: String, tag: String = "အထွေထွေ") {
        val newNote = NoteItem(
            title = title.ifBlank { "မှတ်စုအသစ်" },
            content = content,
            dateText = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
            tag = tag
        )
        _uiState.update { it.copy(notes = listOf(newNote) + it.notes) }
        saveNotes()
    }

    private fun decodeMemory(value: String): String = runCatching {
        String(android.util.Base64.decode(value, android.util.Base64.DEFAULT), Charsets.UTF_8)
    }.getOrDefault(value)

    private fun searchMemory(query: String): Triple<Int, List<String>, String> {
        val q = query.trim().lowercase()
        val noteHits = _uiState.value.notes.filter {
            it.title.lowercase().contains(q) || it.content.lowercase().contains(q) || it.tag.lowercase().contains(q)
        }
        val chatHits = _uiState.value.chatMessages.filter { it.text.lowercase().contains(q) }.takeLast(8)
        val items = buildList {
            noteHits.take(6).forEach { add("📝 ${it.title}: ${it.content.replace('\n', ' ').take(90)}") }
            chatHits.takeLast(6).forEach { add("💬 ${it.text.replace('\n', ' ').take(100)}") }
        }
        val total = noteHits.size + chatHits.size
        val response = if (items.isEmpty()) {
            "\"$query\" နဲ့ကိုက်ညီတဲ့ မှတ်စု/အရင်ပြောထားတာ မတွေ့သေးပါဘူးခင်ဗျာ။"
        } else {
            "\"$query\" နဲ့ကိုက်ညီတာ $total ခု တွေ့ပါတယ်ခင်ဗျာ။\n\n" + items.take(8).joinToString("\n")
        }
        return Triple(total, items.take(8), response)
    }

    fun deleteNote(id: String) {
        _uiState.update { it.copy(notes = it.notes.filterNot { n -> n.id == id }) }
        saveNotes()
    }

    // Spreadsheet
    fun updateSpreadsheetCell(cellKey: String, value: String) {
        _uiState.update {
            val updated = it.spreadsheetCells.toMutableMap()
            updated[cellKey] = value
            it.copy(spreadsheetCells = updated, selectedCellKey = cellKey)
        }
    }

    fun clearSpreadsheet() {
        _uiState.update { it.copy(spreadsheetCells = emptyMap()) }
    }

    // Code Editor
    fun newCodeFile(name: String, language: String) {
        val file = CodeFile(name = name, language = language, content = "// New $language file\n")
        _uiState.update {
            it.copy(
                codeFiles = it.codeFiles + file,
                activeFileIndex = it.codeFiles.size,
                recentFileNames = listOf(name) + it.recentFileNames
            )
        }
    }

    fun updateActiveFileContent(newContent: String) {
        _uiState.update {
            val list = it.codeFiles.toMutableList()
            if (it.activeFileIndex in list.indices) {
                list[it.activeFileIndex] = list[it.activeFileIndex].copy(content = newContent, isModified = true)
            }
            it.copy(codeFiles = list)
        }
    }

    fun selectCodeFile(index: Int) {
        if (index in _uiState.value.codeFiles.indices) {
            _uiState.update { it.copy(activeFileIndex = index) }
        }
    }

    fun saveActiveCodeFile() {
        _uiState.update {
            val list = it.codeFiles.toMutableList()
            if (it.activeFileIndex in list.indices) {
                list[it.activeFileIndex] = list[it.activeFileIndex].copy(isModified = false)
            }
            val fileName = list.getOrNull(it.activeFileIndex)?.name ?: "file"
            it.copy(
                codeFiles = list,
                terminalLogs = it.terminalLogs + "\n[Saved] $fileName successfully saved."
            )
        }
    }

    fun runActiveCode() {
        val active = _uiState.value.codeFiles.getOrNull(_uiState.value.activeFileIndex) ?: return
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val simulationResult = when (active.language) {
            "kotlin" -> ">>> Compiling ${active.name} with kotlinc...\n>>> [Output]: မင်္ဂလာပါ ခေတ်သစ် နည်းပညာ!\n>>> [Output]: Active Engine: Arkar Lin Thit AI\n>>> Process finished with exit code 0"
            "python" -> ">>> python3 ${active.name}\n>>> Executing Arkar Smart Automation...\n>>> Tool Flashlight synced.\n>>> Tool Camera synced.\n>>> Tool Music synced.\n>>> Tool Timer synced.\n>>> Execution OK."
            else -> ">>> Rendering ${active.name} Preview...\n>>> Status: 200 OK. UI Render complete."
        }

        _uiState.update {
            it.copy(
                isTerminalOpen = true,
                terminalLogs = it.terminalLogs + "\n[$timestamp] RUN ${active.name}\n$simulationResult"
            )
        }
    }

    fun toggleTerminal() {
        _uiState.update { it.copy(isTerminalOpen = !it.isTerminalOpen) }
    }

    // Smart Automation Routine Execution
    fun runAutomationRoutine(routine: AutomationRoutine) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    mascotEmotion = MascotEmotion.EXCITED,
                    toolTransitionNotice = "Smart Automation: '${routine.title}' ကို စတင်နေပါသည်..."
                )
            }
            speakVoice("${routine.title} ကို အလိုအလျောက် စတင်လုပ်ဆောင်နေပါပြီခင်ဗျာ။")
            delay(1500)
            selectScreen(routine.targetTool)
        }
    }

    // Dynamic Generative UI
    fun generateFreshDynamicUi() {
        _uiState.update {
            it.copy(
                dynamicUiCards = MiniOfflineAi.generateRandomDynamicUiCards(),
                activeScreen = ActiveToolScreen.DYNAMIC_GENERATED_UI
            )
        }
        showToolAnimationNotice(ActiveToolScreen.DYNAMIC_GENERATED_UI)
    }

    // Settings Updates
    fun updateAiModel(model: AiModelChoice) {
        _uiState.update { it.copy(aiModel = model) }
        prefs.edit().putString("ai_model", model.name).apply()
    }
    fun updateCustomApiKey(key: String) {
        _uiState.update { it.copy(customApiKey = key) }
        prefs.edit().putString("api_key", key).apply()
    }
    fun updateApiEndpoint(url: String) {
        _uiState.update { it.copy(apiEndpoint = url) }
        prefs.edit().putString("endpoint", url).apply()
    }
    fun updateAiModelName(name: String) {
        _uiState.update { it.copy(aiModelName = name) }
        prefs.edit().putString("model_name", name).apply()
    }
    fun updateToolEnabled(id: String, enabled: Boolean) {
        _uiState.update { it.copy(enabledTools = it.enabledTools.toMutableMap().apply { put(id, enabled) }) }
        prefs.edit().putBoolean("tool_$id", enabled).apply()
    }
    fun updateRequireConfirmation(enabled: Boolean) {
        _uiState.update { it.copy(requireConfirmation = enabled) }
        prefs.edit().putBoolean("require_confirmation", enabled).apply()
    }
    fun updateWakeWord(enabled: Boolean, phrase: String = "အာကာလင်းသစ်") {
        voiceManager?.wakeWordEnabled = enabled
        voiceManager?.currentWakeWord = phrase
        _uiState.update { it.copy(wakeWordEnabled = enabled, wakeWordPhrase = phrase) }
        prefs.edit().putBoolean("wake_word_enabled", enabled).putString("wake_word_phrase", phrase).apply()
    }
    fun updateVoiceFeedback(enabled: Boolean) {
        _uiState.update { it.copy(voiceFeedbackEnabled = enabled) }
        prefs.edit().putBoolean("voice_feedback", enabled).apply()
    }
    fun updateThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
        timerJob?.cancel()
        stopwatchJob?.cancel()
        slideshowJob?.cancel()
        musicJob?.cancel()
    }
    fun newChat() {
        val greeting = ChatMessage(
            sender = ChatSender.AI,
            text = "မင်္ဂလာပါခင်ဗျာ! Chat အသစ် စတင်ထားပါတယ်။ ဘာလုပ်ပေးရမလဲ ပြောနိုင်ပါတယ်။"
        )
        _uiState.update { it.copy(chatMessages = listOf(greeting), promptInput = "", activeScreen = ActiveToolScreen.HOME) }
        saveChatHistory()
    }

}
