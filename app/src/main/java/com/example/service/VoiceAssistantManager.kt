package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceAssistantManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit = {},
    private val onWakeWordDetected: () -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var restarting = false
    var isListening = false
        private set
    var wakeWordEnabled = true
    var currentWakeWord = "အာကာလင်းသစ်"

    init { initTextToSpeech() }

    private fun createRecognizer(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return false
        return runCatching {
            speechRecognizer?.destroy()
            speechRecognizer = if (android.os.Build.VERSION.SDK_INT >= 31) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(listener)
            true
        }.getOrElse {
            Log.e("VoiceAssistant", "Recognizer init failed: ${it.message}")
            speechRecognizer = null
            false
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { isListening = true; restarting = false }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) { onRmsChanged(rmsdB) }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { isListening = false }
        override fun onError(error: Int) {
            isListening = false
            // Avoid tight restart loops on permanent errors/cancel events.
            if (error != SpeechRecognizer.ERROR_CLIENT && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS && !restarting) {
                restarting = true
                mainHandler.postDelayed({ if (!isListening) startListeningInternal() }, 450)
            }
        }
        override fun onResults(results: Bundle?) {
            isListening = false
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty().trim()
            if (text.isNotBlank()) checkWakeWordOrExecute(text)
            // Keep foreground voice mode alive for the next utterance.
            if (!restarting) {
                restarting = true
                mainHandler.postDelayed({ if (!isListening) startListeningInternal() }, 250)
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            if (wakeWordEnabled && containsWakeWord(text)) onWakeWordDetected()
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun containsWakeWord(text: String): Boolean {
        val t = text.lowercase()
        return t.contains(currentWakeWord.lowercase()) || t.contains("ဟေး အာကာ") || t.contains("အာကာ") || t.contains("arkar")
    }

    private fun checkWakeWordOrExecute(spokenText: String) {
        if (wakeWordEnabled && containsWakeWord(spokenText)) {
            onWakeWordDetected()
            val cleaned = spokenText
                .replace(currentWakeWord, "", ignoreCase = true)
                .replace("ဟေး အာကာ", "", ignoreCase = true)
                .replace("အာကာ", "", ignoreCase = true)
                .replace("arkar", "", ignoreCase = true)
                .trim()
            onSpeechResult(cleaned.ifBlank { "မင်္ဂလာပါ" })
        } else onSpeechResult(spokenText)
    }

    private fun initTextToSpeech() {
        runCatching {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val my = textToSpeech?.setLanguage(Locale("my", "MM")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    if (my == TextToSpeech.LANG_MISSING_DATA || my == TextToSpeech.LANG_NOT_SUPPORTED) textToSpeech?.language = Locale.US
                    isTtsInitialized = true
                }
            }
        }.onFailure { Log.e("VoiceAssistant", "TTS init failed: ${it.message}") }
    }

    fun startListening(): Boolean {
        if (isListening) return true
        if (speechRecognizer == null && !createRecognizer()) return false
        return startListeningInternal()
    }

    private fun startListeningInternal(): Boolean {
        val recognizer = speechRecognizer ?: return false
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "my-MM")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "my-MM")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        return runCatching {
            restarting = false
            recognizer.startListening(intent)
            isListening = true
            true
        }.getOrElse {
            Log.e("VoiceAssistant", "startListening failed: ${it.message}")
            isListening = false
            false
        }
    }

    fun stopListening() {
        restarting = true
        mainHandler.removeCallbacksAndMessages(null)
        runCatching { speechRecognizer?.cancel() }
        isListening = false
    }

    fun speak(text: String) {
        if (!isTtsInitialized) return
        runCatching {
            val clean = text.replace(Regex("[#*_`\\[\\]()~>]"), "").replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
            textToSpeech?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "ArkarTTS")
        }.onFailure { Log.e("VoiceAssistant", "TTS speak failed: ${it.message}") }
    }

    fun stopSpeaking() { runCatching { textToSpeech?.stop() } }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        runCatching { speechRecognizer?.cancel(); speechRecognizer?.destroy(); speechRecognizer = null; textToSpeech?.shutdown(); textToSpeech = null }
        isListening = false
    }
}
