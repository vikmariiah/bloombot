package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import project.team36.model.ai.ClaudeApi
import project.team36.model.ai.ClaudeMessage
import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ContentBlock
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import kotlin.jvm.java


class AiIntegrationTest {

    private val testApiKey = "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"


    // Builds a Retrofit client configured for the Anthropic API
    private val claudeApi: ClaudeApi by lazy {
        val contentType = "application/json".toMediaType()

        val jsonConfig = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonConfig.asConverterFactory(contentType))
            .build()
            .create(ClaudeApi::class.java)
    }


    // Integration test that verifies the Claude API is reachable and returns a valid response
    @Test
    fun testClaudeApiConnection(): Unit = runBlocking {
        println("Starter test av Claude API-tilkobling...")


        val testRequest = ClaudeRequest(
            model = "claude-sonnet-4-5",
            max_tokens = 100,
            messages = listOf(
                ClaudeMessage(
                    role = "user",
                    content = listOf(ContentBlock(type = "text",
                        text = "Hei Claude! Svar kun med ordet 'Suksess' hvis systemet fungerer."))
                )
            )
        )

        try {
            val response = claudeApi.sendMessage(testApiKey, testRequest)

            assertNotNull("API-kallet returnerte null. Sjekk internett og API-nøkkel.", response)
            assertTrue("Svaret fra Claude mangler innhold.", response.content.isNotEmpty())

            val svarTekst = response.content.firstOrNull { it.type == "text" }?.text

            println("Fikk svar fra Claude: $svarTekst")
            println("Stop reason: ${response.stop_reason}")

            assertNotNull("Klarte ikke å parse tekst fra svaret.", svarTekst)

        } catch (e: Exception) {
            println("Test feilet! Feilmelding: ${e.message}")
            throw e
        }
    }
}