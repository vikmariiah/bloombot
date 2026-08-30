package project.team36.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import project.team36.model.location.SavedPlant

//Database queries for saved plants
@Dao
interface SavedPlantDao {
    @Insert
    suspend fun insert(plant: SavedPlant)

    @Delete
    suspend fun delete(plant: SavedPlant)

    @Update
    suspend fun update(plant: SavedPlant)

    //Gets all plants for a location
    @Query("SELECT * FROM saved_plants WHERE locationId = :locationId")
    fun getPlantsForLocation(locationId: Long): Flow<List<SavedPlant>>

    //Retrieves all plants that have been saved to database
    @Query("SELECT * FROM saved_plants")
    fun getAllPlants(): Flow<List<SavedPlant>>
}