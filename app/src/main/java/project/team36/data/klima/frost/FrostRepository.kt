package project.team36.data.klima.frost

//Repository for fetching weather data from Frost API
class FrostRepository {
    private val clientId = "6ea441ee-0aaf-4c85-9547-815ab276ccef"

    suspend fun getPrecipitationSinceLastUsed(
        lat: Double,
        lon: Double,
        lastWatered: String
    ): Map<String, Double>? {
        return getPrecipitationSinceLastUsed(lat, lon, clientId, lastWatered)
    }
}
