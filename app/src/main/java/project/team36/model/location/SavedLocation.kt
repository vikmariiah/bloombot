package project.team36.model.location

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Saved Location model for database
@Entity(tableName = "saved_location")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "latitude") val lat: Double?,
    @ColumnInfo(name = "longitude") val lon: Double?,
    @ColumnInfo(name = "address") val address: String?,
    @ColumnInfo(name = "zone") val zone: Int = 0
)