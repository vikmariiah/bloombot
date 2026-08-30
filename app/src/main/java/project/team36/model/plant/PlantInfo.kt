package project.team36.model.plant

import kotlinx.serialization.Serializable

enum class SoilType { SOIL, CHALK, CLAY, LOAM, SAND }
enum class LightType { SHADE, PARTIAL, FULL }

// Model for how plant objects are saved
@Serializable
data class PlantInfo(
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
    )

// Translates enum class types to displayable norwegian names
val SoilType.displayName: String
    get() = when (this) {
        SoilType.SOIL -> "Jord"
        SoilType.CHALK -> "Kalkjord"
        SoilType.CLAY -> "Leire"
        SoilType.LOAM -> "Leirjord"
        SoilType.SAND -> "Sand"
    }

val LightType.displayName: String
    get() = when(this) {
        LightType.SHADE -> "Skygge"
        LightType.PARTIAL -> "Delvis skygge"
        LightType.FULL -> "Solrikt"
    }