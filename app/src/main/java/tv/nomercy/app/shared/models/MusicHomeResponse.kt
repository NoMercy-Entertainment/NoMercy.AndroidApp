package tv.nomercy.app.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicHomeResponse(
    val id: String,
    val data: List<Component> = emptyList()
)

