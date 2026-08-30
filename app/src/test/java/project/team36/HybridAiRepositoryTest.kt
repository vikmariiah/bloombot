package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import io.mockk.mockk
import project.team36.model.ai.ClaudeApi
import project.team36.data.mcp.HybridAiRepository
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetMcpClient
import project.team36.model.ai.ClaudeRequest
import project.team36.model.ai.ClaudeResponse
import project.team36.model.ai.ContentBlock
import project.team36.model.location.SavedLocation


class HybridAiRepositoryTest {

    // Verifies that a well-formed JSON response from Claude is correctly parsed into a recommendation object
    @Test
    fun testGetPlantRecommendations_ParsesJsonCorrectly(): Unit = runBlocking {
        println("Starter test av JSON-parsing i HybridAiRepository...")

        val mockMcpClient = mockk<MetMcpClient>()
        val mockLocationRepo = mockk<LocationRepository>()

        val fakeClaudeApi = object : ClaudeApi {
            override suspend fun sendMessage(apiKey: String, request: ClaudeRequest): ClaudeResponse {
                val fakeJson = """
                    {
                      "plantAdvice": [
                        { "plantName": "Rose", "advice": "Husk å vanne rikelig i dag." }
                      ],
                      "generalTasks": ["Rak løv", "Sjekk for ugress"]
                    }
                """.trimIndent()

                return ClaudeResponse(
                    content = listOf(ContentBlock(type = "text", text = fakeJson)),
                    stop_reason = "end_turn"
                )
            }
        }

        val repository = HybridAiRepository(
            claudeApi = fakeClaudeApi,
            mcpClient = mockMcpClient,
            plantDataSource = mockLocationRepo,
            claudeApiKey = "test-key"
        )

        val testLocation = SavedLocation(id = 1L, name = "Testhagen", lat = 59.9, lon = 10.7, address = "", zone = 1)
        val response = repository.getPlantRecommendations(testLocation)
        assertNotNull("JSON-parsingen feilet, returnerte null!", response)

        response?.let {
            println("--- Test results ---")
            println("Første planteråd: ${it.plantAdvice[0].plantName} - ${it.plantAdvice[0].advice}")
            println("---------------------")

            assertEquals(1, it.plantAdvice.size)
            assertEquals("Rose", it.plantAdvice[0].plantName)
            assertEquals("Husk å vanne rikelig i dag.", it.plantAdvice[0].advice)
            assertEquals(2, it.generalTasks.size)
            assertEquals("Rak løv", it.generalTasks[0])
            assertEquals("Sjekk for ugress", it.generalTasks[1])
        }
    }

    // Verifies that the repository returns null when Claude responds with plain text instead of valid JSON
    @Test
    fun testClaudeHallucination_InvalidJsonReturnsNull(): Unit = runBlocking {
        println("Starter test av AI-hallusinasjon (ugyldig JSON)...")

        val mockMcpClient = mockk<MetMcpClient>()
        val mockLocationRepo = mockk<LocationRepository>()

        val hallucinatingClaudeApi = object : ClaudeApi {
            override suspend fun sendMessage(apiKey: String, request: ClaudeRequest): ClaudeResponse {
                val badJson = "Hei! Det er en nydelig dag ute. Et godt tips er å vanne rosene dine i dag."

                return ClaudeResponse(
                    content = listOf(ContentBlock(type = "text", text = badJson)),
                    stop_reason = "end_turn"
                )
            }
        }

        val repository = HybridAiRepository(
            claudeApi = hallucinatingClaudeApi,
            mcpClient = mockMcpClient,
            plantDataSource = mockLocationRepo,
            claudeApiKey = "test-key"
        )

        val testLocation = SavedLocation(id = 1L, name = "Krasjhagen", lat = 59.9, lon = 10.7, address = "", zone = 1)
        val response = repository.getPlantRecommendations(testLocation)


        println("Fikk svar fra repository: $response")

        assertNull("Repositoryet skal returnere null når AI-en svarer med ugyldig JSON format.", response)
    }
}