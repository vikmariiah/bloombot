package project.team36.ui.map

//Configuration to get climate zone data from Klimasonekart (permission to use obtained in March 2026) - https://klimasonekart.no/
object ClimateZoneConfig {
        const val SOURCE_ID = "klimasoner"
        const val TILE_URL = "https://kart.klimasonekart.no/data/Klimasonekart2025/{z}/{x}/{y}.pbf"
        const val LAYER_ID = "klimasoner-lag"
        const val SOURCE_LAYER = "Klimasonekart2025"
}