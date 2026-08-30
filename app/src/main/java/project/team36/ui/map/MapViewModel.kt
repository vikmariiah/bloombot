package project.team36.ui.map

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.data.mcp.MetWeatherRepository
import project.team36.data.mcp.WeatherAlert
import project.team36.model.location.SavedLocation
import project.team36.ui.map.ClimateZoneConfig.LAYER_ID
import kotlin.coroutines.resume
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import java.util.Locale

data class MapUiState(
    val centerLng: Double = 9.30,
    val centerLat: Double = 63.59,
    val zoom: Double = 4.3,
    val searchQuery: String = "",
    val suggestions: List<SearchSuggestion> = emptyList(),
    val selectedResult: SearchResult? = null,
    val showSaveDialog: Boolean = false,
    val placeNameInput: String = "",
    val selectedSavedLocation: SavedLocation? = null,
    val weatherAlerts: List<WeatherAlert> = emptyList(),
    val locationAlerts: List<WeatherAlert> = emptyList(),
    val isAlertLoading: Boolean = false,
    val userLat: Double? = null,
    val userLon: Double? = null,
    val liveLocationPending: Boolean = false,
    val showLiveLocationConfirm: Boolean = false,
    val isSaving: Boolean = false
)


class MapViewModel(
    private val weatherRepository: MetWeatherRepository,
    private val savedLocationsRepository: LocationRepository
) : ViewModel() {

    //startmap centered on Norway
    val mapViewportState = MapViewportState().apply {
        setCameraOptions {
            zoom(4.3)
            center(Point.fromLngLat(9.30, 63.59))
            pitch(0.0)
            bearing(0.0)
        }
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    private var hasInitializedCamera = false //so the camera doesn't zoom around a lot when user is moving around

    //prevents alerts from getting fetched every time the user moves
    private var hasLoadedAlerts = false
    private val searchEngine = SearchEngine.createSearchEngine(
        ApiType.SEARCH_BOX,
        SearchEngineSettings()
    )


    fun updateUserPosition(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(userLat = lat, userLon = lon)
        if (!hasLoadedAlerts) {
            //fetches alerts the first time
            hasLoadedAlerts = true
            fetchAlerts(lat, lon)
        } else {
            //when alerts are already fetched, then it filters through the alerts that applies to the users new position
            val currentAlerts = _uiState.value.weatherAlerts.filter { alert ->
                alert.polygons.any { polygon -> weatherRepository.isPointInPolygon(lat, lon, polygon) }
            }
            _uiState.value = _uiState.value.copy(locationAlerts = currentAlerts)
        }
        if (_uiState.value.liveLocationPending) {
            _uiState.value = _uiState.value.copy(
                liveLocationPending = false,
                showLiveLocationConfirm = true
            )
        }
    }

    //when the user wants to save place based on live location, and it triggers dialogbox to confirm save
    fun requestLiveLocationSave() {
        val lat = _uiState.value.userLat
        val lon = _uiState.value.userLon
        if (lat != null && lon != null) {
            _uiState.value = _uiState.value.copy(showLiveLocationConfirm = true)
        } else {
            _uiState.value = _uiState.value.copy(liveLocationPending = true)
        }
    }


    //fetches addresses based on coordinates, is used for saving based on live location
    //helper function for confirmSaveLiveLocation
    suspend fun getAddress(lat: Double, lon: Double, context: Context): String? {
        return suspendCancellableCoroutine { continuation ->
            //uses android studio's own geocoder class to get address based on coordinates (this requires min API 33, that's why our app is min 33)
            val geocoder = Geocoder(context, Locale.getDefault())
            val listener =  Geocoder.GeocodeListener { results ->
                continuation.resume(results.firstOrNull()?.getAddressLine(0)) //gets first address result
            }
            geocoder.getFromLocation(lat, lon, 1, listener)
        }
    }

    //triggers when the user confirms that they want to save place based on live location
    fun confirmSaveLiveLocation(context: Context, onSaved: () -> Unit) {
        val lat = _uiState.value.userLat ?: return
        val lon = _uiState.value.userLon ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            //calls getAddress to get address from coordinates
            val address = getAddress(lat, lon, context)

            //Switches from followPuck to static camera position to fetch climate zone
            mapViewportState.flyTo(
                cameraOptions {
                    center(Point.fromLngLat(lon, lat))
                    zoom(14.0)
                }
            )
            //delay to give time for the climate zone to be loaded in
            delay(900)
            val zone = getClimateZone(lng = lon, lat = lat)

            savedLocationsRepository.insertLocation(
                SavedLocation(
                    name = "Min posisjon",
                    lat = lat,
                    lon = lon,
                    address = address,
                    zone = zone ?: 0 //fallback to zone 0 if failed to get correct climate zone
                )
            )
            _uiState.value = _uiState.value.copy(isSaving = false, showLiveLocationConfirm = false)
            onSaved()
        }
    }

    fun dismissLiveLocationConfirm() {
        _uiState.value = _uiState.value.copy(showLiveLocationConfirm = false,
            liveLocationPending = false)
    }

    fun onLocationReady() {
        if (!hasInitializedCamera) {
            hasInitializedCamera = true
            mapViewportState.transitionToFollowPuckState()
        }
    }

    //fetches all weather alerts and filters them based on given coordinate
    fun fetchAlerts(userLat: Double, userLon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAlertLoading = true)
            try {
                val allAlerts = weatherRepository.getAllAlerts()
                //filters alerts to only the polygons that contains the users location
                val local = allAlerts.filter { alert ->
                    alert.polygons.any { polygon -> weatherRepository.isPointInPolygon(userLat, userLon, polygon) }
                }
                _uiState.value = _uiState.value.copy(
                    weatherAlerts = allAlerts,
                    locationAlerts = local,
                    isAlertLoading = false
                )
            } catch (e: Exception) {
                Log.e("ALERTS", "Error when fetching alerts: ${e.message}")
                _uiState.value = _uiState.value.copy(isAlertLoading = false)
            }
        }
    }

    //for the searchBar, search suggestions changes as the user is typing
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        //starts showing searchResults when the user has typed 2 letters
        if (query.length >= 2) {
            searchEngine.search(
                query,
                SearchOptions(limit = 6), //limits suggestions to avoid overwhelming the user
                object : SearchSuggestionsCallback {
                    override fun onSuggestions(
                        suggestions: List<SearchSuggestion>,
                        responseInfo: ResponseInfo
                    ) {
                        _uiState.value = _uiState.value.copy(suggestions = suggestions)
                        //for debugging, logs suggestions name and description
                        suggestions.forEachIndexed { index, suggestion ->
                            Log.d(
                                "Suggestion[$index]", """
                                Name: ${suggestion.name}
                                Description: ${suggestion.descriptionText ?: "N/A"}
                            """.trimIndent()
                            )
                        }
                    }

                    override fun onError(e: Exception) {
                        Log.e("MapViewModel", "Search error", e)
                    }
                }
            )
        } else {
            //clears suggestions when the searchBar is almost empty
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
        }
    }


    //when the user clicks on a suggestion while searching, triggers savedialogbox
    fun handleSuggestionSelection(
        suggestion: SearchSuggestion,
        mapViewportState: MapViewportState,
    ) {
        searchEngine.select(suggestion, object : SearchSelectionCallback {
            override fun onResult(
                suggestion: SearchSuggestion,
                result: SearchResult,
                responseInfo: ResponseInfo,
            ) {
                //updates the state with selected results and triggers the save dialogbox
                _uiState.value = _uiState.value.copy(
                    selectedResult = result,
                    showSaveDialog = true,
                    searchQuery = result.name,
                    suggestions = emptyList() //dismisses the suggestions after selecting
                )

                //moves the camera to the selected location on the map
                val camera = cameraOptions {
                    center(result.coordinate)
                    zoom(14.0)
                }
                val animationOptions = MapAnimationOptions.Builder()
                    .duration(2500L)
                    .build()
                mapViewportState.flyTo(camera, animationOptions)
            }

            //unused callbacks, required by mapbox (gives error otherwise)
            override fun onResults(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo) {}
            override fun onError(e: Exception) {}
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {}
        })
    }

    private var mapboxMap: MapboxMap? = null

    fun onMapReady(map: MapboxMap) {
        mapboxMap = map
    }

    //fetches climate zone for given coordinate
    suspend fun getClimateZone(
        lng: Double,
        lat: Double
    ): Int? = suspendCancellableCoroutine { continuation ->
        val map = mapboxMap ?: run {
            //if the map is not ready yet, returns null instead of crashing
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        //converts coordinates to screen pixels, queryrenderedFeatures requires it
        val pointOnScreen = map.pixelForCoordinate(
            Point.fromLngLat(lng, lat)
        )

        //uses the rendered layer at the pixel position to find climate zone
        map.queryRenderedFeatures(
            RenderedQueryGeometry(pointOnScreen),
            RenderedQueryOptions(listOf(LAYER_ID), null)
        ) { result ->
            val zone = result.value
                ?.firstOrNull()
                ?.queriedFeature
                ?.feature
                ?.getNumberProperty("Klasse")
                ?.toInt()
            continuation.resume(zone)
        }
    }

    //triggered when the users confirms saving a location
    fun confirmSavePlace(onSaved: () -> Unit) {
        val result = _uiState.value.selectedResult ?: return
        val name = _uiState.value.placeNameInput.ifEmpty { result.name } //fallback if user left input empty
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            //delay to wait for climate zone features to finish rendering before saving location
            delay(700)
            val zone = getClimateZone(
                lng = result.coordinate.longitude(),
                lat = result.coordinate.latitude()
            )

            //legger inn lokasjonen til repository med koordinater, navn, og addresse
            //saves location in the repository with coordinates, name, address and climate zone
            savedLocationsRepository.insertLocation(
                SavedLocation(
                    name = name,
                    lat = result.coordinate.latitude(),
                    lon = result.coordinate.longitude(),
                    address = result.fullAddress,
                    zone = zone ?: 0 //defaults to 0 if climate zone failed to load
                )
            )
            //closes the save dialog
            _uiState.value = _uiState.value.copy(isSaving = false)
            dismissSaveDialog()
            onSaved()
        }
    }
    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false, placeNameInput = "")
    }

    fun onSavedLocationClicked(location: SavedLocation) {
        _uiState.value = _uiState.value.copy(selectedSavedLocation = location)
    }

    fun dismissPlaceInfo() {
        _uiState.value = _uiState.value.copy(selectedSavedLocation = null)
    }
}



class MapViewModelFactory(
    private val weatherRepository: MetWeatherRepository,
    private val savedLocationsRepository: LocationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(weatherRepository, savedLocationsRepository ) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel klasse")
    }
}