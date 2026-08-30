package project.team36

import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository
import project.team36.data.local.LocationRepository
import project.team36.model.location.SavedPlant
import project.team36.model.plant.LightType
import project.team36.model.plant.SoilType
import project.team36.ui.map.LocationsViewModel
import project.team36.ui.myplants.MyPlantsViewModel
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class MyPlantsViewModelTest {

    val fakeLocationRepo : LocationRepository = mockk<LocationRepository>(relaxed = true)
    val fakeLocationsViewModel = mockk<LocationsViewModel>(relaxed = true)
    val fakeFrostRepo : FrostRepository = mockk<FrostRepository>(relaxed = true)


    // Verifies that daysUntilWatering returns the correct remaining days based on last watered date
    @Test
    fun testDaysUntilWatering () {

        val viewModel = MyPlantsViewModel(fakeLocationRepo, fakeLocationsViewModel,fakeFrostRepo)

        val now = System.currentTimeMillis()
        val daysAgo = 2L
        val plant = opprettTestPlante().copy(
            lastWateredDate = now - TimeUnit.DAYS.toMillis(daysAgo),
            maxDaysWithoutWater = 3
        )

        val result = viewModel.daysUntilWatering(plant)

        println("Dager til vannig: $result")
        assert(result == 1L) {"Forventet 1 dag til vanning, testresultat: $result"}
    }

    // Verifies that removePlantByName finds the correct plant and calls deletePlant on the repository
    @Test
    fun deletingPlantWithRemovePlantByName() = runBlocking {
        val plant = opprettTestPlante()

        every { fakeLocationsViewModel.selectedLocation } returns MutableStateFlow(null)
        every { fakeLocationRepo.getPlantsForLocation(any()) } returns flowOf(listOf(plant))

        val myPlantsViewModel = MyPlantsViewModel(fakeLocationRepo, fakeLocationsViewModel, fakeFrostRepo)

        myPlantsViewModel.plants.filter { it.isNotEmpty() }.first()
        myPlantsViewModel.removePlantByName("Pelargonia")

        coVerify { fakeLocationRepo.deletePlant(plant) }
    }

    // Reusable test plant used across multiple tests
    private fun opprettTestPlante() = SavedPlant(
        id = 1,
        locationId = 1,
        name = "Pelargonia",
        soil = setOf(SoilType.SAND),
        light = setOf(LightType.FULL),
        waterPerWeek = 2f,
        plantingMonth = setOf(5,6),
        climateZone = 3,
        hasBeenWatered = false,
        daysSinceWatered = 0,
        maxDaysWithoutWater = 3,
        imageRes = 0,
        description = "Pelargonia testplante"
    )

}