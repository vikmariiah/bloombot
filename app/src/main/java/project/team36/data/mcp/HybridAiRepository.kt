package project.team36.data.mcp

import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.first
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedLocation
import project.team36.model.ai.*
import kotlinx.serialization.json.Json
import android.util.Log
import project.team36.model.location.SavedPlant
import java.time.LocalDate
import project.team36.model.ai.ClaudeApi



// We want to make sure the user keeps the advice recieved for a day
private data class CachedEntry(
    val response: AiRecommendationResponse,
    val timestamp: Long
)
/*
*  Repository that initializes the conversation between METMcpClient, Sonnet 4.5 and our
*  APIs
*/
class HybridAiRepository(
    private val claudeApi: ClaudeApi,
    private val mcpClient: MetMcpClient,
    private val plantDataSource: LocationRepository,
    private val claudeApiKey: String
) {

    companion object {
        private const val MODEL_NAME = "claude-sonnet-4-5"
        private const val STOP_REASON_TOOL_USE = "tool_use"
        private const val MAX_TOOL_LOOPS = 3
        private const val LOG_TAG_AI_ERROR = "AI_ERROR"
        private const val TOOL_NAME_WEATHER_FORECAST = "weatherforecast"
        private const val TOOL_NAME_WEATHER_ALERTS = "get_weather_alerts"

        private val MARKDOWN_JSON_REGEX = Regex("```json|```")

        private const val CACHE_EXPIRATION_MS = 12 * 60 * 60 * 1000L
    }

    private val dailyCache = mutableMapOf<Long, CachedEntry>()

    // Defines the weathertool so that the AI know what parametres (lat/lon) are required
    private val weatherTool = ClaudeTool(
        name = TOOL_NAME_WEATHER_FORECAST,
        description = "Henter værinformasjon for i dag de kommende dagene.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = mapOf(
                "latitude" to PropertyDefinition("number", "Breddegrad"),
                "longitude" to PropertyDefinition("number", "Lengdegrad")
            ),
            required = listOf("latitude", "longitude")
        )
    )

    // Defines tools for extreme weather
    private val alertTool = ClaudeTool(
        name = TOOL_NAME_WEATHER_ALERTS,
        description = "Henter aktive ekstremværvarsler fra Meteorologisk institutt for Norge. Bruk dette for å advare brukeren om farlige værforhold.",
        input_schema = ToolInputSchema(
            type = "object",
            properties = emptyMap(),
            required = emptyList()
        )
    )

    // Flexsible Json format
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // Fetches the selected gardens plants
    suspend fun getPlantData (location: SavedLocation, plantDataSource: LocationRepository): List<SavedPlant> {
        val plants = try {
            plantDataSource.getPlantsForLocation(location.id).first()
        } catch (_: Exception) {
            emptyList()
        }
        return plants
    }

    // Formating of the plantlist to a readable string for the AI
    suspend fun getPlantTxt(location: SavedLocation, plantDataSource: LocationRepository): String {
        val plantsText = if (getPlantData(location, plantDataSource).isEmpty()) {
            "Brukeren har foreløpig ingen planter på denne lokasjonen. Gi generelle hageråd for dette området."
        } else {
            getPlantData(location, plantDataSource).take(15).joinToString("\n") {
                "- ${it.name}: Sone: ${it.climateZone}, Lysbehov: ${it.light}, Vann Per Uke: ${it.waterPerWeek}"
            }
        }
        return plantsText
    }

    // Checks if advice has already been given
    private fun isCacheValid(entry: CachedEntry?): Boolean {
        if (entry == null) return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - entry.timestamp) < CACHE_EXPIRATION_MS
    }


    // Uses weatherdata and plantinformetion to promt our AI to generate advice in JSON format
    suspend fun getPlantRecommendations(location: SavedLocation, forceRefresh: Boolean = false): AiRecommendationResponse? {
        val lat = location.lat ?: 0.0
        val lon     = location.lon ?: 0.0
        val plantsText = getPlantTxt(location, plantDataSource)
        val currentDate = LocalDate.now().toString()
        val locId = location.id
        val cached = dailyCache[locId]

        if (!forceRefresh && isCacheValid(cached)) {
            Log.d("AI_CACHE", "Returnerer lagret dagsråd for lokasjon: $locId")
            return cached?.response
        }

        // Context prompt that defines a goal, a role and format for the AI.
        val systemPrompt = """
            Du er en profesjonell, presis og velformulert hageekspert i Norge. Din oppgave er å gi skreddersydde, trygge og praktiske hageråd til en hobbygartner.

            KONTEKST FOR DAGEN:
            Dagens dato er: $currentDate. 
            Lokasjon (koordinater): Lat $lat, Lon $lon.

            Du har tilgang til verktøyet MetWeather. DU MÅ bruke dette verktøyet for å hente dagens faktiske vær på denne lokasjonen.

            Brukerens registrerte planter på denne lokasjonen:
            $plantsText

            ABSOLUTTE REGLER FOR RÅDGIVNING (Følg disse for å unngå systemfeil):
    
            1. TIDSFORSTÅELSE OG SESONG: Det er kritisk at du forstår hvilken måned og årstid vi er i basert på datoen ($currentDate). Du skal ALDRI foreslå oppgaver som er ulogiske for sesongen.

            2. FAKTABASERT VÆR: Du skal ALDRI gjette eller hallusinere været. Du må utelukkende basere rådene dine på værdataene du henter fra verktøyet ditt, og planteinformasjon du får fra plantene lagret på lokasjonen.

            3. HÅNDTERING AV PLANTER (Kritisk logikk for respons): 
               - HVIS LISTEN MED PLANTER IKKE ER TOM FOR GJELDENE LOKASJONEN: Du SKAL inkludere nøyaktig ett unikt og praktisk råd for HVER ENESTE plante i listen.
               - HVIS LISTEN MED PLANTER ER TOM FOR GJELDENE LOKASJON: Du må foreslå 2-3 anbefalte planter. I 'Recommend' under 'PlantAdvice' må det inneholde nøyaktig denne setningen: "Siden du enda ikke har valgt noen planter for denne lokasjonen, er dette noen arter vi sterkt anbefaler å vurdere nå."
               
            4. GENERELLE OPPGAVER: 'generalTasks' skal inneholde 2-3 overordnede, sesongriktige hageoppgaver som ikke er knyttet til én spesifikk plante.

            STRENGT JSON-FORMAT (KRITISK SYSTEMKRAV):
            Systemet som leser svaret ditt forventer RÅ JSON. Nøklene i objektet må ALDRI endres. Hvis du bryter disse reglene, krasjer appen:
            - Du MÅ returnere svaret ditt UTELUKKENDE som et gyldig JSON-objekt.
            - INGEN introduksjonstekst før JSON-objektet.
            - INGEN markdown-formatering (IKKE bruk ```json).
            - Unngå bruk av uescapede anførselstegn inne i selve tekstverdiene.

            Svaret ditt skal starte med '{' og slutte med '}', og matche denne strukturen nøyaktig hver gang:
            {
              "plantAdvice": [
                { 
                  "Recommend" : "Setning hvis det IKKE er planter på lokasjonen",
                  "plantName": "Navn på eksisterende eller anbefalt plante", 
                  "advice": "Spesifikt og handling-orientert råd for i dag." 
                }
              ],
              "generalTasks": [
                "Generell oppgave 1", 
                "Generell oppgave 2"
              ]
            }
        """.trimIndent()

        // Start of advice prompt
        val conversation = mutableListOf(
            ClaudeMessage("user", listOf(ContentBlock(type = "text", text = "Hva bør jeg gjøre i hagen nå? Svar KUN med JSON.")))
        )

        return try {
            // Tools gived to claude are initialized
            var response = claudeApi.sendMessage(
                apiKey = claudeApiKey,
                request = ClaudeRequest(
                    model = MODEL_NAME,
                    system = systemPrompt,
                    messages = conversation,
                    tools = listOf(weatherTool, alertTool)
                )
            )

            var toolLoops = 0
            // We want to let the AI loop over the tool to make sure it can give a good reply
            while (response.stop_reason == STOP_REASON_TOOL_USE && toolLoops < MAX_TOOL_LOOPS) {
                toolLoops++

                conversation.add(ClaudeMessage("assistant", response.content))
                val toolUseBlocks = response.content.filter { it.type == "tool_use" }
                val toolResultBlocks = mutableListOf<ContentBlock>()

                // Saves the tools our AI requests to use
                for (block in toolUseBlocks) {
                    val toolId = block.id ?: continue
                    val toolName = block.name ?: ""

                    // Fetches coordinates for the location
                    val inputJson = block.input?.jsonObject
                    val rLat = inputJson?.get("latitude")?.jsonPrimitive?.double ?: lat
                    val rLon = inputJson?.get("longitude")?.jsonPrimitive?.double ?: lon

                    // Runs functions that our AI requests
                    val resultText: String = when (toolName) {
                        TOOL_NAME_WEATHER_FORECAST -> {
                            val weatherRepo = MetWeatherRepository(mcpClient)
                            weatherRepo.getForecast(rLat, rLon) ?: "Kunne ikke hente værvarsel."
                        }
                        TOOL_NAME_WEATHER_ALERTS -> {
                            val weatherRepo = MetWeatherRepository(mcpClient)
                            val alerts = weatherRepo.getAllAlerts()
                            if (alerts.isEmpty()) "Ingen aktive ekstremværvarsler."
                            else alerts.joinToString("\n") { "${it.event}: ${it.description}" }
                        }
                        else -> {
                            Log.w(LOG_TAG_AI_ERROR, "Ukjent verktøy: $toolName")
                            "Ukjent verktøy."
                        }
                    }

                    // Packages the reults in a format readable for the AI
                    toolResultBlocks.add(
                        ContentBlock(type = "tool_result", tool_use_id = toolId, content = resultText)
                    )
                }

                // Puts the tool results in a reply to the user
                conversation.add(ClaudeMessage("user", toolResultBlocks))

                // Sends the results to our AI so it can format a final response
                response = claudeApi.sendMessage(
                    apiKey = claudeApiKey,
                    request = ClaudeRequest(
                        model = MODEL_NAME,
                        system = systemPrompt,
                        messages = conversation,
                        tools = listOf(weatherTool, alertTool)
                    )
                )
            }

            // Pulls out the answer from our AI
            val rawText = response.content.firstOrNull { it.type == "text" }?.text ?: return null

            // Cleanup JSON markdowns if there are any
            val cleanJson = rawText.replace(MARKDOWN_JSON_REGEX, "").trim()
            // Converts the JSON string to our dataclass
            val parsedData = jsonParser.decodeFromString<AiRecommendationResponse>(cleanJson)
            Log.d("AI_RAW", "Dette svarte Claude før parsing:\n$cleanJson")

            dailyCache[locId] = CachedEntry(
                response = parsedData,
                timestamp = System.currentTimeMillis()
            )
            return parsedData

        } catch (e: Exception) {
            Log.e(LOG_TAG_AI_ERROR, "Feil ved parsing av AI-JSON: ${e.message}")
            null
        }
    }

}
