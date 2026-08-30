package project.team36.model.frost

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Wrapper for a paginated list of weather stations returned by the API
@Serializable
data class SourceResponse(
    val data: List<Source>,
    val totalItemCount: Int? = null,
    val apiVersion: String? = null
)

// Model for a weatherstation
@Serializable
data class Source(
    val id: String,
    val name: String,
    val geometry: Point,
    val masl: Double? = null,
    val municipality: String? = null,
    val county: String? = null
)

// GeoJSON-style coordinate holder for a station's geographic position
@Serializable
data class Point(
    @SerialName("@type") val type: String? = null,
    val coordinates: List<Double>
)

// A station's recorded data at a specific point in time, containing one or more measured values
@Serializable
data class Observation (
    val sourceId : String,
    val referenceTime: String,
    val observations : List <ObservationValue>
)


@Serializable
data class ObservationValue (
    val elementId : String,
    val value : Double,
    val unit : String,
    val timeOffset : String ? = null

)

@Serializable
data class ObservationResponse (
    val data : List <Observation>
)