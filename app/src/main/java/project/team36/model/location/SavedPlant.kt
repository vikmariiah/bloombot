package project.team36.model.location

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType


@Entity(
    tableName = "saved_plants",
    foreignKeys = [ForeignKey(
        entity = SavedLocation::class,
        parentColumns = ["id"],
        childColumns = ["locationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["locationId"])]
)
// Saved Location model for plants
data class SavedPlant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val name: String,
    val soil: Set<SoilType>,
    val light: Set<LightType>,
    val waterPerWeek: Float,
    val plantingMonth: Set<Int>,
    val climateZone: Int,
    val hasBeenWatered: Boolean,
    val daysSinceWatered: Int,
    val maxDaysWithoutWater: Int,
    val imageRes: Int,
    val description: String,
    val lastWateredDate: Long = System.currentTimeMillis(),
    val lastFertilizationDate: Long = 0L
)