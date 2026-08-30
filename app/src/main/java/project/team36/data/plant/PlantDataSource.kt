package project.team36.data.plant

import android.content.Context
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantDataSource(private val context: Context) {

    // Reads our CSV with all plantinfo and turns it a list of lists
    fun fetchPlants(): List<PlantInfo> {
        val plants = mutableListOf<PlantInfo>()

        context.assets.open("planter.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split("/")
                if (tokens.size >= 8) {
                    plants.add(mapToPlant(tokens))
                }
            }
        }
        return plants
    }

    // Helper function for splitting lines and formating the information
    @Suppress("DiscouragedApi")
    private fun mapToPlant(tokens: List<String>): PlantInfo {
        val rawName = tokens[0].trim()
        val normalizedName = normalizeName(rawName)
        return PlantInfo(
            name = rawName,
            soil = tokens[1].trim().split(";").map { SoilType.valueOf(it.trim().uppercase()) }.toSet(),
            light = tokens[2].trim().split(";").map { LightType.valueOf(it.trim().uppercase()) }.toSet(),
            waterPerWeek = tokens[3].trim().toFloat(),
            plantingMonth = tokens[4].trim().split(";").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            climateZone = tokens[5].trim().toInt(),
            hasBeenWatered = false,
            daysSinceWatered = 0,
            maxDaysWithoutWater = tokens[6].trim().toInt(),
            imageRes = context.resources.getIdentifier(normalizedName, "drawable", context.packageName),
            description = tokens[7].trim(),
        )
    }

    //we need support for Norwegian lettering
    private fun normalizeName(name: String): String {
        return name
            .replace("æ", "ae").replace("Æ", "ae")
            .replace("ø", "oe").replace("Ø", "oe")
            .replace("å", "aa").replace("Å", "aa")
            .lowercase()
            .replace(" ", "_")
    }
}