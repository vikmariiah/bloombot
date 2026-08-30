package project.team36.ui.plant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

//ViewModel to edit UI and connect with the data
class PlantDetailsViewModel (
    private val plantRepository: PlantRepository,
) : ViewModel() {
    private val _plant = MutableStateFlow<PlantDetails?>(null)
    val plant : StateFlow<PlantDetails?> = _plant.asStateFlow()


    // Loads an unowned plant from the repository (used when browsing/adding a new plant)
    fun loadPlant(plantName: String) {
        _plant.value = plantRepository.initiatePlants()
            .find { it.name == plantName }
            ?.toPlantDetails()
    }

    // Loads an already-saved plant from the current list (used when viewing an existing plant)
    fun loadSavedPlant(plantName: String, plants: List<SavedPlant>) {
        _plant.value = plants
            .find{ it.name == plantName }
            ?.toPlantDetails()
    }
}

data class PlantDetails(
    val name: String,
    val description: String,
    val imageRes: Int,
    val climateZone: Int,
    val waterPerWeek: Float,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val plantingMonth: Set<Int>,
    val maxDaysWithoutWater: Int,
    val id: Long? = null,
    val locationId: Long? = null,
    val hasBeenWatered: Boolean? = null,
    val daysSinceWatered: Int? = null,
    val lastWateredDate: Long? = null,
    val lastFertilizationDate: Long? = null
)

fun PlantInfo.toPlantDetails() = PlantDetails(
    name = name,
    description = description,
    imageRes = imageRes,
    climateZone = climateZone,
    waterPerWeek = waterPerWeek,
    soil = soil,
    light = light,
    plantingMonth = plantingMonth,
    maxDaysWithoutWater = maxDaysWithoutWater
)

fun SavedPlant.toPlantDetails() = PlantDetails(
    name = name,
    description = description,
    imageRes = imageRes,
    climateZone = climateZone,
    waterPerWeek = waterPerWeek,
    soil = soil,
    light = light,
    plantingMonth = plantingMonth,
    maxDaysWithoutWater = maxDaysWithoutWater,
    lastWateredDate = lastWateredDate,
    locationId = locationId
)

class PlantDetailsViewModelFactory(
    private val plantRepository: PlantRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlantDetailsViewModel(plantRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}