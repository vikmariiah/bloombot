package project.team36.ui.landingpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherAlert
import project.team36.data.mcp.WeatherForecast
import project.team36.model.location.SavedLocation
import kotlin.jvm.java
import project.team36.data.mcp.HybridAiRepository
import project.team36.model.ai.AiRecommendationResponse

//ViewModel to edit UI and connect with the data
class LandingPageViewModel(
    private val weatherRepository: MetWeatherRepository,
    private val aiRepository: HybridAiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LandingPageUiState())
    val uiState: StateFlow<LandingPageUiState> = _uiState.asStateFlow()

    //fetches ai advice for given location
    fun fetchAiAdvice(location: SavedLocation) {
        _uiState.value = _uiState.value.copy(isAiLoading = true, aiResponse = null)
        viewModelScope.launch {
            val response = aiRepository.getPlantRecommendations(location)
            _uiState.value = _uiState.value.copy(isAiLoading = false, aiResponse = response)
        }
    }

    fun dismissAiAdvice() {
        _uiState.value = _uiState.value.copy(aiResponse = null)
    }

    fun showInfoDialog() {
        _uiState.value = _uiState.value.copy(showInfoDialog = true)
    }

    fun dismissInfoDialog() {
        _uiState.value = _uiState.value.copy(showInfoDialog = false)
    }

    //fetches weather forecasts and MetAlerts for all saved locations (using couroutine)
    fun loadDataForLocations(locations: List<SavedLocation>) {
        //uses courotine so all weather data can be fetched at the same time
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true, isAlertsLoading = true)

            //fetches all alerts once so it can be reused for all the saved locations
            val allAlerts = runCatching { weatherRepository.getAllAlerts() }.getOrDefault(emptyList())

            val alertsMap = mutableMapOf<Long, List<WeatherAlert>>()
            val forecastsMap = mutableMapOf<Long, WeatherUiState?>()

            //goes through each saved location and fetches data for it
            locations.forEach { location ->
                val lat = location.lat ?: 0.0
                val lon = location.lon ?: 0.0

                //filters those alerts that are relevant for this locations coordinates
                val relevantAlerts = allAlerts.filter { alert ->
                    alert.polygons.any { polygon ->
                        weatherRepository.isPointInPolygon(lat, lon, polygon)
                    }
                }
                alertsMap[location.id] = relevantAlerts

                //gets and parses weather forecast
                val rawForecast = runCatching {
                    weatherRepository.getForecast(lat, lon)
                }.getOrNull()
                forecastsMap[location.id] = rawForecast
                    ?.let { weatherRepository.getParsedForecast(it) }
                    ?.toUiState()
            }

            _uiState.value = _uiState.value.copy(
                locationForecasts = forecastsMap,
                locationAlerts = alertsMap,
                isWeatherLoading = false,
                isAlertsLoading = false
            )
        }
    }
}

data class WeatherUiState(
    val temperature: String,
    val precipitation: String,
    val description: String,
    val iconName: String
)

data class LandingPageUiState(
    val locationForecasts: Map<Long, WeatherUiState?> = emptyMap(),
    val isWeatherLoading: Boolean = false,
    val locationAlerts : Map <Long, List<WeatherAlert>> = emptyMap(),
    val isAlertsLoading : Boolean = false,
    val aiResponse: AiRecommendationResponse? = null,
    val isAiLoading: Boolean = false,
    val showInfoDialog: Boolean = false
)

//maps weather symbols from met to weather descriptions in norwegian
private fun weatherSymbolToDescription(symbol: String): String {
    return when (symbol) {
        "clearsky", "clearsky_day", "clearsky_night", "clearsky_polartwilight" -> "Klarvær"
        "fair", "fair_day", "fair_night", "fair_polartwilight" -> "Lettskyet"
        "partlycloudy", "partlycloudy_day", "partlycloudy_night", "partlycloudy_polartwilight" -> "Delvis skyet"
        "cloudy" -> "Overskyet"
        "fog" -> "Tåke"

        "rain" -> "Regn"
        "rainandthunder" -> "Regn og torden"
        "rainshowers_day", "rainshowers_night", "rainshowers_polartwilight" -> "Regnbyger"
        "rainshowersandthunder_day", "rainshowersandthunder_night", "rainshowersandthunder_polartwilight" -> "Regnbyger med torden"

        "lightrain" -> "Lett regn"
        "lightrainandthunder" -> "Lett regn og torden"
        "lightrainshowers_day", "lightrainshowers_night", "lightrainshowers_polartwilight" -> "Lette regnbyger"
        "lightrainshowersandthunder_day", "lightrainshowersandthunder_night", "lightrainshowersandthunder_polartwilight" -> "Lette regnbyger med torden"

        "heavyrain" -> "Kraftig regn"
        "heavyrainandthunder" -> "Kraftig regn og torden"
        "heavyrainshowers", "heavyrainshowers_day", "heavyrainshowers_night", "heavyrainshowers_polartwilight" -> "Kraftige regnbyger"
        "heavyrainshowersandthunder_day", "heavyrainshowersandthunder_night", "heavyrainshowersandthunder_polartwilight" -> "Kraftige regnbyger med torden"

        "sleet" -> "Sludd"
        "sleetandthunder" -> "Sludd og torden"
        "sleetshowers_day", "sleetshowers_night", "sleetshowers_polartwilight" -> "Sluddbyger"
        "sleetshowersandthunder_day", "sleetshowersandthunder_night", "sleetshowersandthunder_polartwilight" -> "Sluddbyger med torden"

        "lightsleet" -> "Lett sludd"
        "lightsleetandthunder" -> "Lett sludd og torden"
        "lightsleetshowers_day", "lightsleetshowers_night", "lightsleetshowers_polartwilight" -> "Lette sluddbyger"
        "lightssleetshowersandthunder_day", "lightssleetshowersandthunder_night", "lightssleetshowersandthunder_polartwilight" -> "Lette sluddbyger med torden"

        "heavysleet" -> "Kraftig sludd"
        "heavysleetandthunder" -> "Kraftig sludd og torden"
        "heavysleetshowers_day", "heavysleetshowers_night", "heavysleetshowers_polartwilight" -> "Kraftige sluddbyger"
        "heavysleetshowersandthunder_day", "heavysleetshowersandthunder_night", "heavysleetshowersandthunder_polartwilight" -> "Kraftige sluddbyger med torden"

        "snow" -> "Snø"
        "snowandthunder" -> "Snø og torden"
        "snowshowers_day", "snowshowers_night", "snowshowers_polartwilight" -> "Snøbyger"
        "snowshowersandthunder_day", "snowshowersandthunder_night", "snowshowersandthunder_polartwilight" -> "Snøbyger med torden"

        "lightsnow" -> "Lett snø"
        "lightsnowandthunder" -> "Lett snø og torden"
        "lightsnowshowers_day", "lightsnowshowers_night", "lightsnowshowers_polartwilight" -> "Lette snøbyger"
        "lightssnowshowersandthunder_day", "lightssnowshowersandthunder_night", "lightssnowshowersandthunder_polartwilight" -> "Lette snøbyger med torden"

        "heavysnow" -> "Kraftig snø"
        "heavysnowandthunder" -> "Kraftig snø og torden"
        "heavysnowshowers_day", "heavysnowshowers_night", "heavysnowshowers_polartwilight" -> "Kraftige snøbyger"
        "heavysnowshowersandthunder_day", "heavysnowshowersandthunder_night", "heavysnowshowersandthunder_polartwilight" -> "Kraftige snøbyger med torden"

        "thunder" -> "Torden"

        else -> symbol //fallback, just shows the raw symbol text
    }
}

//converts weatherforecast to strings that can easily be used in weathercard
private fun WeatherForecast.toUiState() = WeatherUiState(
    temperature = "${temperature}°C",
    precipitation = "🌧 $precipitation mm",
    description = weatherSymbolToDescription(weatherSymbol),
    iconName = weatherSymbol
)


class LandingPageViewModelFactory(
    private val weatherRepository: MetWeatherRepository,
    private val aiRepository: HybridAiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LandingPageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LandingPageViewModel(weatherRepository, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}