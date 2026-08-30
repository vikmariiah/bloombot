package project.team36

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import project.team36.data.plant.PlantDataSource
import project.team36.data.plant.PlantRepository
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.PlantInfo
import project.team36.model.plant.SoilType

class PlantRepositoryTest {

    private val mockDataSource = mockk<PlantDataSource>(relaxed = true)
    private lateinit var plantRepository: PlantRepository

    // 2 reusable fake plants one is a plant object and the other is tied to a location
    private fun testPlant(name: String, climateZone: Int) = PlantInfo(
        name = name,
        climateZone = climateZone,
        soil = setOf(SoilType.SOIL),
        light = setOf(LightType.FULL),
        waterPerWeek = 1.0f,
        plantingMonth = setOf(5),
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 7,
        imageRes = 0,
        description = ""
    )

    private fun testSavedPlant(name: String, locationId: Long = 1L) = SavedPlant(
        locationId = locationId,
        name = name,
        soil = setOf(SoilType.SOIL),
        light = setOf(LightType.FULL),
        waterPerWeek = 1.0f,
        plantingMonth = setOf(5),
        climateZone = 4,
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 7,
        imageRes = 0,
        description = ""
    )

    // reusable fake location
    private fun testLocation() = SavedLocation(
        name = "TestHage",
        lat = 60.0,
        lon = 10.0,
        address = "Gateveien",
        zone = 4
    )

    // Sets up a plant list spanning zones 2, 4, and 6 to test zone filtering logic
    @Before
    fun setup() {
        val plants = listOf(
            testPlant("Rose", climateZone = 4),
            testPlant("Tulipan", climateZone = 6),
            testPlant("Palme", climateZone = 2)
        )
        every { mockDataSource.fetchPlants() } returns plants
        plantRepository = PlantRepository(mockDataSource)
    }

    @Test
    fun initiatePlants_returnsAllPlants() {
        val result = plantRepository.initiatePlants()
        assertEquals(3, result.size)
    }

    // Plants with a climate zone >= location zone should be recommended
    @Test
    fun testPlantsSuitableForZone() {
        val result = plantRepository.recommendPlants(testLocation(), emptyList())

        assertTrue("Rose (climateZone 4) skal anbefales for sone 4", result.any { it.name == "Rose" })
        assertTrue("Tulipan (climateZone 6) skal anbefales for sone 4", result.any { it.name == "Tulipan" })
        assertFalse("Palme (climateZone 2) skal ikke anbefales for sone 4", result.any { it.name == "Palme" })
    }

    // Already saved plants should be excluded from recommendations even if zone-compatible
    @Test
    fun testAlreadySavedPlants() {
        val savedPlants = listOf(testSavedPlant("Rose"))
        val result = plantRepository.recommendPlants(testLocation(), savedPlants)

        assertFalse("Rose er allerede lagret og skal ekskluderes", result.any { it.name == "Rose" })
    }

    // Plants with a climate zone below the location zone should appear in notRecommended
    @Test
    fun testPlantsTooWarmForZone() {
        val result = plantRepository.notRecommendedPlants(testLocation(), emptyList())

        assertTrue("Palme (climateZone 2) skal være i andre planter for sone 4", result.any { it.name == "Palme" })
        assertFalse("Rose (climateZone 4) skal ikke være i andre planter", result.any { it.name == "Rose" })
        assertFalse("Tulipan (climateZone 6) skal ikke være i andre planter", result.any { it.name == "Tulipan" })
    }

    // Already saved plants should be excluded from notRecommended as well
    @Test
    fun testExcludesAlreadySavedPlantsNotRecommend() {
        val savedPlants = listOf(testSavedPlant("Palme"))
        val result = plantRepository.notRecommendedPlants(testLocation(), savedPlants)

        assertFalse("Palme er allerede lagret og skal ekskluderes", result.any { it.name == "Palme" })
    }
}