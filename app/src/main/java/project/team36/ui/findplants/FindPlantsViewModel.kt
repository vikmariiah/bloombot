package project.team36.ui.findplants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import project.team36.data.local.LocationRepository
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.ui.plant.PlantDetails
import project.team36.ui.plant.toPlantDetails

//ViewModel to edit UI and connect with the data
class FindPlantsViewModel (
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantDetails>>(emptyList())
    val plants: StateFlow<List<PlantDetails>> = _plants.asStateFlow()
    private val _notRecommendedPlants = MutableStateFlow<List<PlantDetails>>(emptyList())
    val notRecommendedPlants: StateFlow<List<PlantDetails>> = _notRecommendedPlants.asStateFlow()

    init {
        _plants.value = plantRepository.initiatePlants().map { it.toPlantDetails() }
    }

    // When user har chosen a plant and location, it's saved to the database
    fun addPlant(plant: PlantDetails, location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.insertPlant(
                SavedPlant(
                    locationId = location.id,
                    name = plant.name,
                    soil = plant.soil,
                    light = plant.light,
                    waterPerWeek = plant.waterPerWeek,
                    plantingMonth = plant.plantingMonth,
                    climateZone = plant.climateZone,
                    hasBeenWatered = false,
                    daysSinceWatered = 0,
                    maxDaysWithoutWater = plant.maxDaysWithoutWater,
                    imageRes = plant.imageRes,
                    description = plant.description,
                    lastWateredDate = 0L
                )
            )
        }
    }
    // Loads recommended and not recommended plants
    fun loadRecommendedPlants(location: SavedLocation) {
        viewModelScope.launch {
            locationRepository.getPlantsForLocation(location.id)
                .first()
                .let { savedPlants ->
                    val recommended = plantRepository.recommendPlants(location, savedPlants)
                        .map { it.toPlantDetails() }
                    _plants.value = recommended

                    val notRecommended = plantRepository.notRecommendedPlants(location, savedPlants)
                        .map { it.toPlantDetails() }
                    _notRecommendedPlants.value = notRecommended
                }
        }
    }
    fun clearPlants() {
        _plants.value = emptyList()
        _notRecommendedPlants.value = emptyList()
    }
}

class FindPlantsViewModelFactory(
    private val plantRepository: PlantRepository,
    private val locationRepository: LocationRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FindPlantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FindPlantsViewModel(plantRepository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}