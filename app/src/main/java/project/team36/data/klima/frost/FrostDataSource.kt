package project.team36.data.klima.frost

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import project.team36.model.frost.ObservationResponse
import project.team36.model.frost.SourceResponse
import project.team36.model.frost.Source
import java.time.Instant

// Data source to get information from Frost API
val client = HttpClient(io.ktor.client.engine.android.Android) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true //
            isLenient = true
        })
    }
}

//Find the nearest station, or return null if none are found
// Doesn't use IFI proxy server towards the API as the TA's said it was not needed
suspend fun getNearestStation(lat: Double, lon: Double, clientId: String): Source? {
    val url = "https://frost.met.no/sources/v0.jsonld"

    return try {
        val response = client.get(url) {
            // Frost requires our client ID as username in basic auth
            basicAuth(clientId, "")

            //We use the nearest funksjonen of Frost API
            parameter("geometry", "nearest(POINT($lon $lat))")
        }

        if (response.status == HttpStatusCode.OK) {
            val sourceResponse: SourceResponse = response.body()
            // Returns the first station in the list
            sourceResponse.data.firstOrNull()
        } else {
            Log.e("FROST", "Errors from API: ${response.status}")
            null
        }
    } catch (e: Exception) {
        Log.e("FROST", "Did not fetch data: ${e.message}")
        null
    }
}

//Get precipitation for location since a plant was last watered until now.
suspend fun getPrecipitationSinceLastUsed(
    lat: Double,
    lon: Double,
    clientId: String,
    lastWatered: String
): Map<String, Double>? {
    val station = getNearestStation(lat, lon, clientId) ?: return null
    val url = "https://frost.met.no/observations/v0.jsonld"
    val now = Instant.now().toString().substringBefore(".") + "Z"

    return try {
        val response = client.get(url) {
            basicAuth(clientId, "")
            parameter("sources", station.id)
            parameter("elements", "sum(precipitation_amount PT1H)")
            parameter("referencetime", "$lastWatered/$now")
        }
        if (response.status == HttpStatusCode.OK) {
            val observationResponse: ObservationResponse = response.body()
            observationResponse.data
                .groupBy { it.referenceTime.substring(0, 10) }
                .mapValues { (_, obs) ->
                    obs.flatMap { it.observations }.sumOf { it.value }
                }
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("FROST", "Exception: ${e.message}")
        null
    }
}