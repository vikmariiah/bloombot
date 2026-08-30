package project.team36

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import project.team36.data.klima.frost.FrostRepository

class FrostRepositoryTest  {

    // Creates a mock of the repository
    private val mockFrostRepo = mockk<FrostRepository> (relaxed = true)

    @Test
    fun testPrecipitationSinceLastUsed() : Unit = runBlocking {
        // Create a map of dates and precipitation
        val mockPrecipitationMap = mapOf(
            "2026-05-01" to 2.5,
            "2026-05-02" to 0.0,
            "2026-05-03" to 5.0
        )
        every {runBlocking {mockFrostRepo.getPrecipitationSinceLastUsed(
            any(),
            any(),
            any()
        )}} returns mockPrecipitationMap

        //Get precipitation history
        val result = mockFrostRepo.getPrecipitationSinceLastUsed(59.9, 10.7,"2026-05-01")

        //Check that the map contains expected data
        assertNotNull(result)
        assertEquals(3, result?.size)
        assertEquals(5.0, result?.get("2026-05-03")!!, 0.1)
        println("Suksess: Fant nedbør for ${result.size} dager")
    }
}