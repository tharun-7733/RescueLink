package com.example.sos

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sos.BuildConfig
import com.example.sos.ai.GeminiContent
import com.example.sos.ai.GeminiPart
import com.example.sos.ai.GeminiRequest
import com.example.sos.ai.GeminiRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Message(val text: String, val isUser: Boolean)

/**
 * ViewModel that drives the TARS AI assistant chat screen.
 *
 * Security model
 * ──────────────
 * • The Gemini API key is loaded from BuildConfig.GEMINI_API_KEY, which Gradle
 *   reads from local.properties at build time.
 * • local.properties is listed in .gitignore — the key is NEVER committed.
 * • The key is never stored as a field in this class; it is passed inline to
 *   each API call so there is no single long-lived string holding it.
 *
 * Concurrency model
 * ─────────────────
 * • All network work runs on Dispatchers.IO via viewModelScope.launch to keep
 *   the main thread free.
 * • State (chatMessages) is a SnapshotStateList updated only on the main
 *   dispatcher (withContext(Dispatchers.Main) is implicit via snapshotStateList
 *   inside viewModelScope).
 */
class ChatViewModel : ViewModel() {

    val chatMessages = mutableStateListOf(
        Message("Hi! I'm Tars. How can I assist you today?", false)
    )

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        chatMessages.add(Message(userMessage, true))

        viewModelScope.launch(Dispatchers.IO) {
            val reply = runCatching {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(userMessage)))
                    )
                )

                // API key injected from BuildConfig — read from local.properties
                val response = GeminiRetrofitClient.service.generateContent(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    body = request
                )

                response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?: "I'm sorry, I couldn't generate a response."

            }.getOrElse { e ->
                "Error: ${e.localizedMessage}"
            }

            withContext(Dispatchers.Main) {
                chatMessages.add(Message(reply, false))
            }
        }
    }
}