import kotlinx.serialization.SerialName
import tv.nomercy.app.shared.models.ColorPalettes
import tv.nomercy.app.shared.models.ComponentData
import tv.nomercy.app.shared.models.ExtendedVideo
import tv.nomercy.app.shared.models.Rating

data class HomeItem(
    @SerialName("id")
    val id: Int,

    @SerialName("backdrop")
    val backdrop: String?,

    @SerialName("overview")
    val overview: String,

    @SerialName("poster")
    val poster: String,

    @SerialName("duration")
    val duration: Int,

    @SerialName("watched")
    val watched: Boolean,

    @SerialName("title")
    val title: String,

    @SerialName("name")
    val name: String?,

    @SerialName("titleSort")
    val titleSort: String,

    @SerialName("type")
    val type: String,

    @SerialName("year")
    val year: Int,

    @SerialName("media_type")
    val mediaType: String,

    @SerialName("content_ratings")
    val contentRatings: List<Rating>,

    @SerialName("tags")
    val tags: List<String>,

    @SerialName("color_palette")
    val colorPalette: ColorPalettes? = null,

    @SerialName("logo")
    val logo: String?,

    @SerialName("favorite")
    val favorite: Boolean?,

    @SerialName("number_of_items")
    val numberOfItems: Int,

    @SerialName("have_items")
    val haveItems: Int,

    @SerialName("videos")
    val videos: List<ExtendedVideo>,

    @SerialName("link")
    val link: String
): ComponentData