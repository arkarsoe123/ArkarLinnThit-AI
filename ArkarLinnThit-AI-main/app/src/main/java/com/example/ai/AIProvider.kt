package com.example.ai

import com.example.model.ChatMessage

data class AiProviderRequest(
    val prompt: String,
    val apiKey: String = "",
    val endpoint: String = "",
    val model: String = "",
    val history: List<ChatMessage> = emptyList()
)

data class AiProviderResponse(
    val text: String,
    val provider: String,
    val model: String,
    val latencyMs: Long
)

interface AIProvider {
    suspend fun generate(request: AiProviderRequest): AiProviderResponse?
}

/**
 * Provider registry used by v1.6.0. The existing ArkarAiEngine remains the
 * network implementation so this layer can be expanded without changing UI.
 */
object AIProviderRegistry {
    val supported = listOf(
        "Mini Offline AI",
        "Gemini",
        "OpenAI-compatible",
        "Anthropic",
        "Ollama / Localhost",
        "Custom API"
    )
}
