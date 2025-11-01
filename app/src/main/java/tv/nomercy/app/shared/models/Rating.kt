package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val id: Int? = null,
    @SerialName("iso_3166_1")
    val iso31661: String,
    val rating: String,
    val meaning: String? = null,
    val order: Int? = null
)