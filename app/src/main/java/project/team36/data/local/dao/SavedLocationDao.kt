package project.team36.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import project.team36.model.location.SavedLocation

// Database queries for saved locations
@Dao
interface SavedLocationDao {
    @Insert
    suspend fun insert(location: SavedLocation): Long

    @Delete
    suspend fun delete(location: SavedLocation)

    //Updates the existing plant
    @Update
    suspend fun update(location: SavedLocation)

    @Query("SELECT * FROM saved_location")
    fun getAllLocations(): Flow<List<SavedLocation>>
}