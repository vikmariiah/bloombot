package project.team36.data.plant

import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.PlantInfo

// Repository for all our information about plants
class PlantRepository(
    private val plantDataSource: PlantDataSource,
) {
    private var cachedPlants: List<PlantInfo> = emptyList()

    fun initiatePlants(): List<PlantInfo> {
        if (cachedPlants.isEmpty()) {
            cachedPlants = plantDataSource.fetchPlants()
        }
        return cachedPlants
    }


    // Recommends plants based on climate zone
    fun recommendPlants(location: SavedLocation, savedPlants: List<SavedPlant>): List<PlantInfo> {
        if (cachedPlants.isEmpty()) cachedPlants = plantDataSource.fetchPlants()
        val savedPlantNames = savedPlants.map { it.name }.toSet()

        return cachedPlants.filter {
            it.climateZone >= location.zone && it.name !in savedPlantNames
        }
    }

    // Opposite of recommend plants
    fun notRecommendedPlants(location: SavedLocation, savedPlants: List<SavedPlant>): List<PlantInfo> {
        if (cachedPlants.isEmpty()) cachedPlants = plantDataSource.fetchPlants()
        val savedPlantNames = savedPlants.map { it.name }.toSet()

        return cachedPlants.filter {
            it.climateZone < location.zone && it.name !in savedPlantNames
        }
    }
}
