package com.example.sos

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {

    // 1. Ensure your API Key is full and valid
    private val apiKey = "AIzaSyChTA8Yeg9GoCr0QxcVic8nprtR5dMdz28"

    val chatMessages = mutableStateListOf<Message>(
        Message("Hi! I'm Tars. How can I assist you today?", false)
    )

    // 2. Initializing with the correct model
    private val generativeModel = GenerativeModel(
        // "gemini-1.5-flash" is correct, but ONLY works with SDK 0.9.0+
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        chatMessages.add(Message(userMessage, true))

        viewModelScope.launch {
            try {
                // Use generateContent
                val response = generativeModel.generateContent(userMessage)

                val reply = response.text ?: "I'm sorry, I couldn't understand that."
                chatMessages.add(Message(reply, false))

            } catch (e: Exception) {
                // If you still get a 404, try changing modelName to "gemini-pro"
                e.printStackTrace()
                chatMessages.add(Message("Error: ${e.localizedMessage}", false))
            }
        }
    }
}