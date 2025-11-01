package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ExtendedVideo(
    val name: String,
    val key: String? = null,
    val src: String,
    val site: String,
    val size: Int,
    val type: String,
    val official: Boolean? = false,
    val id: String? = null,

    @SerialName("iso_639_1")
    val iso6391: String? = null,

    @SerialName("iso_3166_1")
    val iso31661: String? = null,

    @SerialName("published_at")
    val publishedAt: String? = null,
)
