package project.team36.data.local

import androidx.room.TypeConverter
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType


// Converts Sets of soiltype etc to make it compatible with saving in the DB

class Converters {
    @TypeConverter
    fun fromSoilTypeSet(value: Set<SoilType>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toSoilTypeSet(value: String): Set<SoilType> = value.split(",").map { SoilType.valueOf(it) }.toSet()

    @TypeConverter
    fun fromLightTypeSet(value: Set<LightType>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toLightTypeSet(value: String): Set<LightType> = value.split(",").map { LightType.valueOf(it) }.toSet()

    @TypeConverter
    fun fromIntSet(value: Set<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntSet(value: String): Set<Int> = value.split(",").map { it.toInt() }.toSet()

}