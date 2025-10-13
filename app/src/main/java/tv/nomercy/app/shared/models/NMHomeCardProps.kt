package tv.nomercy.app.shared.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NMHomeCard")
data class NMHomeCardProps(

    val id: FlexibleId,
    val title: String,
//    val titleSort: String,
    val overview: String? = null,
    val link: String,
    val rating: String? = null,
    val year: Int? = null,
    val duration: Int? = null,
//    val type: String,
    val backdrop: String? = null,
    val poster: String? = null,
    val logo: String? = null,

    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,

    @SerialName("content_ratings")
    val contentRatings: List<Rating> = emptyList(),

    @SerialName("have_items")
    val haveItems: Int? = null,

    @SerialName("number_of_items")
    val numberOfItems: Int? = null,

    @SerialName("media_type")
    val mediaType: String? = null,

    val videos: List<ExtendedVideo> = emptyList(),
    val videoID: String? = null,
): ComponentData