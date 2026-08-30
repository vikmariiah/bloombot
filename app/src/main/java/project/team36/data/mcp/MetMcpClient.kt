package project.team36.data.mcp

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

//MCP-client structure from https://github.com/modelcontextprotocol/kotlin-sdk
class MetMcpClient {
    private val httpClient = HttpClient(Android) { install(SSE) }
    private var isConnected = false
    private val connectMutex = Mutex()

    private val client = Client(
        clientInfo = Implementation(
            name = "plant-app",
            version = "1.0.0"
        )
    )

    suspend fun connect() {
        connectMutex.withLock {
            if (isConnected) return
            try {
                val transport = StreamableHttpClientTransport(
                    client = httpClient,
                    url = "https://webapi.met.no/mcp-server"
                )
                client.connect(transport)
                isConnected = true

            } catch (e: Exception) {
                isConnected = false
                throw Exception("Kunne ikke koble til MET MCP-server: ${e.message}")
            }
        }
    }

    //Fetch forecast as text
    suspend fun getForecast(latitude: Double, longitude: Double): String {
        val result = client.callTool(
            CallToolRequest(
                params = CallToolRequestParams(
                    name = "forecast",
                    arguments = buildJsonObject {
                        put("latitude", JsonPrimitive(latitude))
                        put("longitude", JsonPrimitive(longitude))
                    }
                )
            )
        )
        return (result.content.firstOrNull() as? TextContent)?.text ?: ""
    }

    //Get alerts as text
    suspend fun getAlerts(): String {
        connect()
        val alertResult = client.callTool(
            CallToolRequest(
                params = CallToolRequestParams(
                    name = "alerts",
                    arguments = buildJsonObject {
                        put("geographic_domain", JsonPrimitive("land"))
                        put("language", JsonPrimitive("no"))
                    }
                )
            )
        )
        return (alertResult.content.firstOrNull() as? TextContent)?.text ?: ""
    }
}