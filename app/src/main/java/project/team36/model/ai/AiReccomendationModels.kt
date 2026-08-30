package project.team36.model.ai

import kotlinx.serialization.Serializable

//Internal data classes for AI responses
@Serializable
data class AiRecommendationResponse(
    val plantAdvice: List<PlantAdvice>,
    val generalTasks: List<String>
)

// Dataclass for advice
@Serializable
data class PlantAdvice(
    val recommend: String? = null,
    val plantName: String,
    val advice: String
)