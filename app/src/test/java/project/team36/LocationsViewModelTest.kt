package project.team36

import kotlinx.coroutines.runBlocking
import org.junit.Test
import project.team36.model.location.SavedLocation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import project.team36.data.local.LocationRepository
import project.team36.ui.map.LocationsViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


class LocationsViewModelTest {
    private val mockRepo = mockk<LocationRepository>(relaxed = true)
    //a testlocation that gets added into the repo
    private fun testLocation() = SavedLocation(
        name = "TestHage",
        lat = 60.0,
        lon = 10.0,
        address = "Gateveien",
        zone = 1
    )

    //tests that the user can add more locations
    @Test
    fun testCanAddMoreLocations(): Unit = runBlocking {
        every { mockRepo.allLocations } returns flowOf(emptyList()) //simulates that savedLocations returns an empty list
        val viewModel = LocationsViewModel(mockRepo)

        assertTrue("Should be able to add a location", viewModel.onAddLocationClicked())
        assertFalse("ShowMaxLocationsDialog should be false", viewModel.showMaxLocationsDialog.value)
    }


    //tests that the user cannot add more locations when they have 5 of them saved
    @Test
    fun testCannotAddMoreLocations(): Unit = runBlocking {
        every { mockRepo.allLocations } returns flowOf(List(5) { testLocation() }) //simulates that there are 5 locations in savedLocations
        val viewModel = LocationsViewModel(mockRepo)
        viewModel.savedLocations.filter {it.size == 5}.first() //filter makes it wait until stateFlow has gotten 5 locations

        assertFalse("Should not be able to add a location", viewModel.onAddLocationClicked())
        assertTrue("ShowMaxLocationsDialog should be true", viewModel.showMaxLocationsDialog.value)
    }
}