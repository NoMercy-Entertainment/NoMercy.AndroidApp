package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Video(
    @SerialName("iso_639_1") val iso6391: String? = null,
    @SerialName("iso_3166_1") val iso31661: String? = null,
    val name: String? = null,
    val key: String? = null,
    val src: String? = null,
    val site: String? = null,
    val size: Int? = null,
    val type: String? = null,
    val official: Boolean? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val id: String? = null
)
