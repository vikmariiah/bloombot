package project.team36.data.local

import kotlinx.coroutines.flow.Flow
import project.team36.data.local.dao.SavedLocationDao
import project.team36.data.local.dao.SavedPlantDao
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant

// Repository for managing all edits to the database
class LocationRepository(
    private val locationDao: SavedLocationDao,
    private val savedPlantDao: SavedPlantDao
) {

    val allLocations: Flow<List<SavedLocation>> = locationDao.getAllLocations()

    suspend fun insertLocation(location: SavedLocation): Long {
        return locationDao.insert(location)
    }

    suspend fun deleteLocation(location: SavedLocation) {
        return locationDao.delete(location)
    }

    suspend fun updateLocation(location: SavedLocation) {
        return locationDao.update(location)
    }

    suspend fun insertPlant(plant: SavedPlant) {
        savedPlantDao.insert(plant)
    }

    fun getPlantsForLocation(locationId: Long): Flow<List<SavedPlant>> {
        return savedPlantDao.getPlantsForLocation(locationId)
    }

    suspend fun deletePlant(plant: SavedPlant) {
        savedPlantDao.delete(plant)
    }

    suspend fun updatePlant(plant: SavedPlant) {
        savedPlantDao.update(plant)
    }

    fun getAllPlants(): Flow<List<SavedPlant>> {
        return savedPlantDao.getAllPlants()
    }
}