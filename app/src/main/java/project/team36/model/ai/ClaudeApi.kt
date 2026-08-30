package project.team36.model.ai

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

// This interface decides the format and the API key for communication with our AI
interface ClaudeApi {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json",
        "anthropic-version: 2023-06-01"
    )
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: ClaudeRequest
    ): ClaudeResponse
}