package com.example.rescuelink.ai

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// ─── Request / Response DTOs ─────────────────────────────────────────────────

data class GeminiRequest(
    @SerializedName("contents") val contents: List<GeminiContent>
)

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?
)

data class Candidate(
    @SerializedName("content") val content: GeminiContent?
)

// ─── Retrofit Interface ───────────────────────────────────────────────────────

interface GeminiApiService {
    /**
     * Send a generateContent request to Gemini.
     * The [apiKey] is passed as a query parameter — injected at call-site from
     * BuildConfig so it is never hard-coded in source.
     */
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): GeminiResponse
}
