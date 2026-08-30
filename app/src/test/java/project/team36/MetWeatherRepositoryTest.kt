package project.team36

import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import project.team36.data.mcp.MetMcpClient
import project.team36.data.mcp.MetWeatherRepository


class TestMetWeatherRepository {

    //mocking a mcp client
    private val mockClient = mockk<MetMcpClient>()
    private val weatherRepository = MetWeatherRepository(mockClient)

    //test polygon
    val polygon = listOf(
        Pair(59.0, 9.0),
        Pair(45.0, 15.0),
        Pair(59.0, 13.0),
        Pair(63.0, 13.0)
    )


    //tests that a point is inside polygon
    @Test
    fun testPointInsidePolygon() {
        val result = weatherRepository.isPointInPolygon(60.0, 10.0, polygon)
        assertTrue("Point (60.0, 10.0) should be inside polygon", result)
    }

    //tests that a point is outside polygon
    @Test
    fun testPointOutsidePolygon() {
        val result = weatherRepository.isPointInPolygon(64.0, 9.0, polygon)
        assertFalse("Point (64.0, 9.0) should be outside polygon", result)
    }

}